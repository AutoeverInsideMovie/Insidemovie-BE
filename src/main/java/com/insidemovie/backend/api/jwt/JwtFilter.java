package com.insidemovie.backend.api.jwt;

import io.jsonwebtoken.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class JwtFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;

    public JwtFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    // 매 요청마다 실행
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException, java.io.IOException {

        // 헤더에서 JWT 추출
        String jwt = resolveToken(request); // 헤더에서 JWT 추출
        System.out.println("JWT: " + jwt);

        // 로그인, 회원가입 요청은 JWT 검사 안 함
        String requestURI = request.getRequestURI();
        if (requestURI.equals("/api/v1/member/login") || requestURI.equals("/api/v1/member/signup") ||
                requestURI.equals("/api/v1/member/kakao-accesstoken") ||   // 추가
                requestURI.equals("/api/v1/member/kakao-login")) {
            filterChain.doFilter(request, response);
            return;
        }
        if (StringUtils.hasText(jwt) && jwtProvider.validateToken(jwt)) {
            Authentication authentication = jwtProvider.getAuthentication(jwt);
            SecurityContextHolder.getContext().setAuthentication(authentication); // 인증 객체 설정

        }

        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }

    // Authorization 헤더에서 토큰 추출
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 부분 제거
        }
        return null;
    }
}
