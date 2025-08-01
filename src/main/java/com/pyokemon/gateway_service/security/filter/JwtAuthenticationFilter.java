package com.pyokemon.gateway_service.security.filter;

import com.pyokemon.gateway_service.security.jwt.JwtTokenValidator;
import com.pyokemon.gateway_service.security.jwt.authentication.JwtAuthentication;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenValidator jwtTokenValidator;
    
    // 공개 API 경로들 (토큰 검증 제외)
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
        "/api/account/login",
        "/api/account/logout",
        // "/api/events/open-today",
        // "/api/events/to-be-opened",
        // "/api/payment/webhook",
        "/health",
        "/actuator",
        "/api/gateway/v1"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        
        if (isPublicPath(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            String jwtToken = jwtTokenValidator.getToken(request);
            
            if (jwtToken != null) {
                JwtAuthentication authentication = jwtTokenValidator.validateToken(jwtToken);
                if (authentication != null) {
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info("JWT 토큰 검증 성공. 사용자: {}", 
                             authentication.getPrincipal().getName());
                } else {
                    log.warn("JWT 토큰 검증 실패");
                }
            } else {
                log.info("JWT 토큰이 없음");
            }
            
        } catch (Exception e) {
            log.error("JWT 토큰 검증 중 오류: {}", e.getMessage(), e);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private boolean isPublicPath(String requestURI) {
        return PUBLIC_PATHS.stream()
                .anyMatch(path -> requestURI.startsWith(path));
    }
}