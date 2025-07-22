package com.insidemovie.backend.common.config.security;

import com.insidemovie.backend.api.jwt.JwtFilter;
import com.insidemovie.backend.api.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable default form login, HTTP Basic, CSRF
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .csrf(AbstractHttpConfigurer::disable)

            // CORS configuration
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Allow H2 console frames
            .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))

            // Stateless session (JWT)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // URL access rules
            .authorizeHttpRequests(auth -> auth
                // Public: H2 console, Swagger
                .requestMatchers(
                    "/h2-console/**",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/swagger-resources/**",
                    "/swagger-ui.html",
                    "/webjars/**",
                    "/api-doc"
                ).permitAll()

                // Public: member endpoints
                .requestMatchers(
                    "/api/v1/member/signup",
                    "/api/v1/member/reissue",
                    "/api/v1/member/login",
                    "/api/v1/member/kakao-accesstoken",
                    "/api/v1/member/kakao-login",
                    "/api/v1/member/kakao-signup",
                    "/api/v1/member/token-reissue",
                    "/api/v1/member/check-nickname",
                    "/api/v1/match/weekly-match",
                    "/api/v1/match/winners"
                ).permitAll()

                // Role-based
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                // USER POST
                .requestMatchers(
                        HttpMethod.POST,
                        "/api/v1/report/**"
                ).hasRole("USER")

                // Public GET
                .requestMatchers(
                        HttpMethod.GET,
                        "/api/v1/movies",
                        "/api/v1/movies/search",
                        "/api/v1/movies/search/**",
                        "/api/v1/movies/recommend/**",
                        "/api/v1/movies/popular",
                        "/api/v1/movies/detail/*",
                        "/api/v1/movies/*/emotion-summary",
                        "/api/v1/movies/emotions/*",
                        "/api/v1/movies/*/reviews",
                        "/api/v1/boxoffice/**"
                ).permitAll()

                // Public POST
                .requestMatchers(
                        HttpMethod.POST,
                        "/api/v1/member/signup/emotion",
                        "/api/v1/recommend/emotion"
                ).permitAll()

                // Public PATCH
                .requestMatchers(
                        HttpMethod.PATCH,
                        "/api/v1/member/emotion/**"
                ).permitAll()
                .requestMatchers(
                        "/api/v1/mail/send/**",
                        "/api/v1/mail/check/**"
                ).permitAll()

                // All other requests require authentication
                .anyRequest().authenticated()
            )

            // JWT filter
            .addFilterBefore(new JwtFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class)

            // Spring Security 로그아웃 기능 비활성화
            .logout(AbstractHttpConfigurer::disable)

            // Return 401 on unauthorized
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            );

        return http.build();
    }

    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(
            "http://localhost:5173",
            "http://localhost:8000"
        ));
        config.setAllowedMethods(Collections.singletonList("*"));
        config.setAllowedHeaders(Collections.singletonList("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);                // 1 hour
        config.addExposedHeader("Authorization");
        config.addExposedHeader("Authorization-Refresh");

        return request -> config;
    }
}
