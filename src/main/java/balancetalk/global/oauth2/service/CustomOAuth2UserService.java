package balancetalk.global.oauth2.service;

import static balancetalk.global.config.SecurityConfig.passwordEncoder;
import static balancetalk.member.domain.Role.USER;
import static balancetalk.member.domain.SignupType.STANDARD;

import balancetalk.global.oauth2.dto.CustomOAuth2User;
import balancetalk.global.oauth2.dto.GoogleResponse;
import balancetalk.global.oauth2.dto.KakaoResponse;
import balancetalk.global.oauth2.dto.NaverResponse;
import balancetalk.global.oauth2.dto.Oauth2Dto;
import balancetalk.global.oauth2.dto.Oauth2Response;
import balancetalk.member.domain.Member;
import balancetalk.member.domain.MemberRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Value("${spring.security.security.oauth2-password}")
    private String oauth2Password;

    @Value("${urls.firstRegister}")
    private String firstRegisterUrl;

    @Value("${urls.alreadyRegistered}")
    private String alreadyRegisteredUrl;

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        Oauth2Response oauth2Response = switch (registrationId) {
            case "naver" -> new NaverResponse(oAuth2User.getAttributes());
            case "google" -> new GoogleResponse(oAuth2User.getAttributes());
            case "kakao" -> new KakaoResponse(oAuth2User.getAttributes());
            default -> null;
        };

        String email = getEmail(oauth2Response);
        Member findMember = memberRepository.findByEmail(email).orElse(null);

        if (findMember == null) {
            String encodedPassword = passwordEncoder().encode(oauth2Password);
            Oauth2Dto oauth2Dto = Oauth2Dto.builder()
                    .name(hideNickname(email))
                    .email(email)
                    .role(USER)
                    .signupType(STANDARD)
                    .password(encodedPassword)
                    .build();

            Member newMember = oauth2Dto.toEntity();
            memberRepository.save(newMember);
            return new CustomOAuth2User(oauth2Dto, firstRegisterUrl);
        }

        else { // 회원이 존재할 떄
            Oauth2Dto oauth2Dto = Oauth2Dto.builder()
                    .name(findMember.getNickname())
                    .email(findMember.getEmail())
                    .role(findMember.getRole())
                    .build();
            if (findMember.getSignupType() == STANDARD) { // 일반 회원일 때 소셜 로그인 클릭 시, 홈으로 리다이렉트
                return new CustomOAuth2User(oauth2Dto, firstRegisterUrl);
            }
            return new CustomOAuth2User(oauth2Dto, alreadyRegisteredUrl);
        }
    }

    private String hideNickname(String nickname) {
        StringBuilder sb = new StringBuilder(nickname);
        for (int i = 3; i < nickname.length(); i++) {
            if (nickname.charAt(i) == '@') {
                break;
            }
            sb.setCharAt(i, '*');
        }
        return sb.toString();
    }

    private String getEmail(Oauth2Response oauth2Response) {
        return Optional.ofNullable(oauth2Response)
                .map(Oauth2Response::getEmail)
                .orElse("null");
    }
}
