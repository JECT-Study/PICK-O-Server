package balancetalk.global.jwt;

import static balancetalk.global.caffeine.CacheType.RefreshToken;
import static balancetalk.global.exception.ErrorCode.CACHE_NOT_FOUND;

import balancetalk.global.exception.BalanceTalkException;
import balancetalk.global.exception.ErrorCode;
import balancetalk.global.oauth2.dto.CustomOAuth2User;
import balancetalk.member.domain.Member;
import balancetalk.member.domain.MemberRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final CacheManager cacheManager;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // Oauth2User
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();

        String email = customUserDetails.getEmail();

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new BalanceTalkException(ErrorCode.NOT_FOUND_MEMBER));
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication, member.getId());

        response.addCookie(JwtTokenProvider.createCookie(refreshToken));

        Optional.ofNullable(cacheManager.getCache(RefreshToken.getCacheName()))
                .ifPresentOrElse(
                        cache -> cache.put(member.getId(), refreshToken),
                        () -> {
                            throw new BalanceTalkException(CACHE_NOT_FOUND);
                        });

        String redirectUrl = customUserDetails.getRedirectUrl();
        response.sendRedirect(redirectUrl);
    }
}
