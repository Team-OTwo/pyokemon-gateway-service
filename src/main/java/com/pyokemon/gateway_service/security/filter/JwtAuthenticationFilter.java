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
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenValidator jwtTokenValidator;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // 정적 공개 경로들
    private static final List<String> STATIC_PUBLIC_PATHS = Arrays.asList(
        "/health",
        "/actuator",
        "/api/gateway/v1"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String requestMethod = request.getMethod();

        if (isPublicPath(requestURI, requestMethod)) {
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

    private boolean isPublicPath(String requestURI, String method) {

         // 임시로 모든 API에 대해 인증 해제
        //return true;
        
        // 정적 공개 경로들
        if (STATIC_PUBLIC_PATHS.stream().anyMatch(requestURI::startsWith)) {
            return true;
        }

        // 로그인 관련 POST 요청 (공개)
        if ("POST".equals(method)) {
            if (requestURI.equals("/account/api/login") ||
                requestURI.equals("/account/api/app/login") ||
                requestURI.equals("/account/api/users") ||
                requestURI.equals("/account/api/tenants")) {
                return true;
            }
        }

        // GET 요청만 공개
        // Todo: api 경로 맞게 수정
        if ("GET".equals(method)) {
            // 정확한 경로 매칭
            if (requestURI.equals("/event/api/events") ||
                requestURI.equals("/event/api/events/open-today") ||
                requestURI.equals("/event/api/events/to-be-opened")) {
                return true;
            }

            // 쿼리 파라미터가 있는 경로
            if (requestURI.startsWith("/event/api/events?") ||
                requestURI.startsWith("/event/api/seats?")) {
                return true;
            }

            // 동적 경로 패턴
            if (pathMatcher.match("/event/api/events/*", requestURI)) {
                return true;
            }
            if (pathMatcher.match("/event/api/event-schedules/*", requestURI)) {
                return true;
            }
        }

        return false; // 기타 모든 요청은 인증 필요
    }
}