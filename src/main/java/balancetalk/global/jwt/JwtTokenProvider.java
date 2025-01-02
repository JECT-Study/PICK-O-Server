package balancetalk.global.jwt;

import balancetalk.global.exception.BalanceTalkException;
import balancetalk.global.exception.ErrorCode;
import balancetalk.member.application.MyUserDetailService;
import balancetalk.member.domain.CustomUserDetails;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Key;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${spring.jwt.token.access-expiration-time}")
    private long accessExpirationTime;

    @Value("${spring.jwt.token.refresh-expiration-time}")
    private long refreshExpirationTime;

    private static final String MEMBER_ID = "memberId";
    private final MyUserDetailService myUserDetailService;
    private final Key secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    /**
     * Access 토큰 생성
     */
    public String createAccessToken(Authentication authentication, Long memberId) {
        validateAuthentication(authentication);
        Claims claims = Jwts.claims();
        claims.put(MEMBER_ID, memberId);
        claims.setSubject(authentication.getName());
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + accessExpirationTime);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Refresh 토큰 생성
     */
    public String createRefreshToken(Authentication authentication, Long memberId) {
        validateAuthentication(authentication);
        Claims claims = Jwts.claims();
        claims.put(MEMBER_ID, memberId);
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + refreshExpirationTime);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(secretKey)
                .compact();
    }

    public static Cookie createCookie(String refreshToken) {
        String cookieName = "refreshToken";
        Cookie cookie = new Cookie(cookieName, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setDomain("pick0.com");
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24); // accessToken 유효
        return cookie;
    }

    public static Cookie createAccessCookie(String accessToken) {
        String cookieName = "accessToken";
        Cookie cookie = new Cookie(cookieName, accessToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setDomain("pick0.com");
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24);
        return cookie;
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public String getRole(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody()
                .get("role", String.class);
    }

    public String getPayload(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new BalanceTalkException(ErrorCode.EXPIRED_JWT_TOKEN);
        } catch (IllegalArgumentException | MalformedJwtException e) {
            throw new BalanceTalkException(ErrorCode.EMPTY_JWT_TOKEN);
        } catch (SignatureException e) {
            throw new BalanceTalkException(ErrorCode.INVALID_JWT_TOKEN);
        }
    }

    private void validateAuthentication(Authentication authentication) {
        if (authentication == null) {
            throw new BalanceTalkException(ErrorCode.NOT_FOUND_MEMBER);
        }
    }


    public String reissueAccessToken(String refreshToken) {
        validateToken(refreshToken);
        Authentication authentication = getAuthenticationByToken(refreshToken);
        CustomUserDetails customUserDetails = myUserDetailService.loadUserByUsername(authentication.getName());
        Long memberId = customUserDetails.getMemberId();
        return createAccessToken(authentication, memberId);
    }

    public Authentication getAuthenticationByEmail(String email) {
        CustomUserDetails customUserDetails = myUserDetailService.loadUserByUsername(email);
        return new UsernamePasswordAuthenticationToken(customUserDetails, "", customUserDetails.getAuthorities());
    }

    public Authentication getAuthenticationByToken(String token) {
        Long memberId = getMemberId(token);
        CustomUserDetails customUserDetails = myUserDetailService.loadByMemberId(memberId);
        return new UsernamePasswordAuthenticationToken(customUserDetails, "", customUserDetails.getAuthorities());
    }

    public Long getMemberId(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
        return claims.get(MEMBER_ID, Long.class);
    }
}
