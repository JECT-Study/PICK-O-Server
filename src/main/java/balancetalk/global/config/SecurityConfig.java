package balancetalk.global.config;

import balancetalk.global.jwt.CustomLogoutHandler;
import balancetalk.global.jwt.JwtAccessDeniedHandler;
import balancetalk.global.jwt.JwtAuthenticationEntryPoint;
import balancetalk.global.jwt.JwtAuthenticationFilter;
import balancetalk.global.jwt.JwtTokenProvider;
import balancetalk.global.jwt.CustomSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    private final long MAX_AGE_SEC = 3600;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final OAuth2UserService oAuth2UserService;
    private final CustomSuccessHandler customSuccessHandler;
    private final CacheManager cacheManager;

    @Bean
    public static BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager (AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(exception -> {
                    exception.authenticationEntryPoint(jwtAuthenticationEntryPoint);
                    exception.accessDeniedHandler(jwtAccessDeniedHandler);
                })
                // 세션 사용 X (jwt 사용)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(request -> request
                        .requestMatchers("/**").permitAll()
                        .anyRequest().authenticated()
                )

                // jwtFilter 먼저 적용
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class)

                .addFilterBefore(new CustomLogoutHandler(jwtTokenProvider, cacheManager),
                        LogoutFilter.class)

                // oauth2
                .oauth2Login((oauth2) ->
                        oauth2.userInfoEndpoint(
                                        userInfoEndpointConfig -> userInfoEndpointConfig.userService(oAuth2UserService))
                                .successHandler(customSuccessHandler) // customSuccessHandler 등록 -> 로그인이 성공하면 쿠키에 담김
                );
    return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("http://localhost:8080");
        configuration.addAllowedOriginPattern("http://localhost:3000"); // 프론트 쪽에서 허용
        configuration.addAllowedOriginPattern("https://pick0.com"); // 도메인 주소
        configuration.addAllowedOriginPattern("https://api.pick0.com"); // 도메인 주소
        configuration.addAllowedHeader("Accept");
        configuration.addAllowedHeader("Authorization");
        configuration.addAllowedHeader("refreshToken");
        configuration.addAllowedHeader("accessToken");
        configuration.addAllowedHeader("Content-Type");
        configuration.addAllowedHeader("Origin");
        configuration.addAllowedHeader("Cookie");
        configuration.addAllowedHeader("X-Requested-With");
        configuration.addAllowedHeader("Access-Control-Allow-Origin");
        configuration.addAllowedHeader("Access-Control-Allow-Credentials");
        configuration.addAllowedHeader("Access-Control-Allow-Methods");
        configuration.addAllowedHeader("Access-Control-Allow-Headers");
        configuration.addAllowedHeader("Host");
        configuration.addAllowedHeader("Connection");
        configuration.addAllowedHeader("Accept-Encoding");
        configuration.addAllowedHeader("Accept-Language");
        configuration.addAllowedHeader("Referer");
        configuration.addAllowedHeader("User-Agent");
        configuration.addAllowedHeader("Sec-Fetch-Mode");
        configuration.addAllowedHeader("Sec-Fetch-Site");
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(MAX_AGE_SEC);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
