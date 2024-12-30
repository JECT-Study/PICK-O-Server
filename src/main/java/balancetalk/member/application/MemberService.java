package balancetalk.member.application;

import static balancetalk.global.caffeine.CacheType.RefreshToken;
import static balancetalk.global.exception.ErrorCode.ALREADY_REGISTERED_EMAIL;
import static balancetalk.global.exception.ErrorCode.ALREADY_REGISTERED_NICKNAME;
import static balancetalk.global.exception.ErrorCode.AUTHENTICATION_REQUIRED;
import static balancetalk.global.exception.ErrorCode.CACHE_NOT_FOUND;
import static balancetalk.global.exception.ErrorCode.FORBIDDEN_MEMBER_DELETE;
import static balancetalk.global.exception.ErrorCode.MISMATCHED_EMAIL_OR_PASSWORD;
import static balancetalk.global.exception.ErrorCode.NOT_FOUND_CACHE_VALUE;
import static balancetalk.global.exception.ErrorCode.NOT_FOUND_FILE;
import static balancetalk.global.exception.ErrorCode.NOT_FOUND_MEMBER;
import static balancetalk.global.exception.ErrorCode.PASSWORD_MISMATCH;
import static balancetalk.global.exception.ErrorCode.SAME_NICKNAME;

import balancetalk.file.domain.File;
import balancetalk.file.domain.FileHandler;
import balancetalk.file.domain.FileType;
import balancetalk.file.domain.repository.FileRepository;
import balancetalk.global.exception.BalanceTalkException;
import balancetalk.global.jwt.JwtTokenProvider;
import balancetalk.member.domain.Member;
import balancetalk.member.domain.MemberRepository;
import balancetalk.member.dto.ApiMember;
import balancetalk.member.dto.MemberDto.JoinRequest;
import balancetalk.member.dto.MemberDto.LoginRequest;
import balancetalk.member.dto.MemberDto.MemberResponse;
import balancetalk.member.dto.MemberDto.MemberUpdateRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final FileRepository fileRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileHandler fileHandler;
    private final CacheManager cacheManager;

    public void join(final JoinRequest joinRequest) {
        if (memberRepository.existsByEmail(joinRequest.getEmail())) {
            throw new BalanceTalkException(ALREADY_REGISTERED_EMAIL);
        }
        if (memberRepository.existsByNickname(joinRequest.getNickname())) {
            throw new BalanceTalkException(ALREADY_REGISTERED_NICKNAME);
        }
        if (!joinRequest.getPassword().equals(joinRequest.getPasswordConfirm())) {
            throw new BalanceTalkException(PASSWORD_MISMATCH);
        }
        joinRequest.setPassword(passwordEncoder.encode(joinRequest.getPassword()));
        Member savedMember = memberRepository.save(joinRequest.toEntity());

        if (joinRequest.hasProfileImgId()) {
            File newProfileImgFile = fileRepository.findById(joinRequest.getProfileImgId())
                    .orElseThrow(() -> new BalanceTalkException(NOT_FOUND_FILE));
            if (newProfileImgFile.isUploadedByMember()) {
                fileHandler.relocateFile(newProfileImgFile, savedMember.getId(), FileType.MEMBER);
            }
        }
    }

    public String login(final LoginRequest loginRequest, HttpServletResponse response) {
        Member member = memberRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new BalanceTalkException(MISMATCHED_EMAIL_OR_PASSWORD));
        if (!passwordEncoder.matches(loginRequest.getPassword(), member.getPassword())) {
            throw new BalanceTalkException(MISMATCHED_EMAIL_OR_PASSWORD);
        }

        Authentication authentication = jwtTokenProvider.getAuthenticationByEmail(loginRequest.getEmail());
        String accessToken = jwtTokenProvider.createAccessToken(authentication, member.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication, member.getId());

        Optional.ofNullable(cacheManager.getCache(RefreshToken.getCacheName()))
                .ifPresentOrElse(
                        cache -> cache.put(member.getId(), refreshToken),
                        () -> {
                            throw new BalanceTalkException(CACHE_NOT_FOUND);
                        });
        Cookie cookie = JwtTokenProvider.createCookie(refreshToken);
        response.addCookie(cookie);
        return accessToken;
    }

    @Transactional(readOnly = true)
    public MemberResponse findById(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BalanceTalkException(NOT_FOUND_MEMBER));

        if (member.getProfileImgId() == null) {
            return MemberResponse.fromEntity(member, null);
        }

        String imgUrl = fileRepository.findById(member.getProfileImgId())
                .orElseThrow(() -> new BalanceTalkException(NOT_FOUND_FILE))
                .getImgUrl();
        return MemberResponse.fromEntity(member, imgUrl);
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> findAll() {
        List<Member> members = memberRepository.findAll();
        return members.stream()
                .map(member -> {
                    if (member.getProfileImgId() == null) {
                        return MemberResponse.fromEntity(member, null);
                    }

                    String imgUrl = fileRepository.findById(member.getProfileImgId())
                            .orElseThrow(() -> new BalanceTalkException(NOT_FOUND_FILE))
                            .getImgUrl();
                    return MemberResponse.fromEntity(member, imgUrl);
                })
                .toList();
    }

    public void delete(final LoginRequest loginRequest, ApiMember apiMember) {
        Member member = apiMember.toMember(memberRepository);
        if (!member.getEmail().equals(loginRequest.getEmail())) {
            throw new BalanceTalkException(FORBIDDEN_MEMBER_DELETE);
        }
        if (!passwordEncoder.matches(loginRequest.getPassword(), member.getPassword())) {
            throw new BalanceTalkException(MISMATCHED_EMAIL_OR_PASSWORD);
        }
        memberRepository.deleteByEmail(member.getEmail());
    }

    public void logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new BalanceTalkException(AUTHENTICATION_REQUIRED);
        }
    }

    public void verifyNickname(String nickname) {
        if (memberRepository.existsByNickname(nickname)) {
            throw new BalanceTalkException(ALREADY_REGISTERED_NICKNAME);
        }
    }

    public String reissueAccessToken(ApiMember apiMember) {
        Member member = apiMember.toMember(memberRepository);
        Cache cache = Optional.ofNullable(cacheManager.getCache(RefreshToken.getCacheName()))
                .orElseThrow(() -> new BalanceTalkException(CACHE_NOT_FOUND));
        ValueWrapper valueWrapper = cache.get(member.getId());
        if (valueWrapper == null) {
            throw new BalanceTalkException(NOT_FOUND_CACHE_VALUE);
        }
        String refreshToken = (String) valueWrapper.get();
        return jwtTokenProvider.reissueAccessToken(refreshToken);
    }

    public void updateMemberInformation(MemberUpdateRequest memberUpdateRequest, ApiMember apiMember) {
        Member member = apiMember.toMember(memberRepository);

        if (memberUpdateRequest.getProfileImgId() != null) {
            if (member.hasProfileImgId()) {
                File oldProfileImgFile = fileRepository.findById(member.getProfileImgId())
                        .orElseThrow(() -> new BalanceTalkException(NOT_FOUND_FILE));
                if (oldProfileImgFile.isUploadedByMember()) {
                    fileHandler.deleteFile(oldProfileImgFile);
                }
            }
            File newProfileImgFile = fileRepository.findById(memberUpdateRequest.getProfileImgId())
                    .orElseThrow(() -> new BalanceTalkException(NOT_FOUND_FILE));
            member.updateImageId(newProfileImgFile.getId());
            if (newProfileImgFile.isUploadedByMember()) {
                fileHandler.relocateFile(newProfileImgFile, member.getId(), FileType.MEMBER);
            }
        }

        if (memberUpdateRequest.getNickname() != null) {
            validateSameNickname(memberUpdateRequest, member);
            member.updateNickname(memberUpdateRequest.getNickname());
        }
    }

    private void validateSameNickname(MemberUpdateRequest memberUpdateRequest, Member member) {
        if (member.getNickname().equals(memberUpdateRequest.getNickname())) {
            throw new BalanceTalkException(SAME_NICKNAME);
        }
    }

    public void verifyPassword(String password, ApiMember apiMember) {
        Member member = apiMember.toMember(memberRepository);

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new BalanceTalkException(PASSWORD_MISMATCH);
        }
    }
}
