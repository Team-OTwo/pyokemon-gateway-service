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
        "/api/gateway/v1",
        "/actuator/prometheus"
    );

    // 계정 관련 공개 경로
    public static class AccountApi {
        public static final List<String> PUBLIC_POST_PATHS = Arrays.asList(
                "/account/api/login",
                "/account/api/app/login",
                "/account/api/users",
                "/account/api/tenants"
        );
    }

    // 이벤트 관련 공개 경로
    public static class EventApi {
        public static final List<String> PUBLIC_GET_EXACT_PATHS = Arrays.asList(
                "/event/api/events",
                "/event/api/events/open-today",
                "/event/api/events/to-be-opened"
        );

        public static final List<String> PUBLIC_GET_PREFIX_PATHS = Arrays.asList(
                "/event/api/events?",
                "/event/api/seats?"
        );

        public static final List<String> PUBLIC_GET_PATTERN_PATHS = Arrays.asList(
                "/event/api/event-schedules/*"
        );
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

//        String requestURI = request.getRequestURI();
//        String requestMethod = request.getMethod();
//
//        if (isPublicPath(requestURI, requestMethod)) {
//            filterChain.doFilter(request, response);
//            return;
//        }

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

//    private boolean isPublicPath(String requestURI, String method) {
//
//        // 임시로 모든 API 인증 해제
//        //return true;
//
//        // 정적 공개 경로들
//        if (STATIC_PUBLIC_PATHS.stream().anyMatch(requestURI::startsWith)) {
//            return true;
//        }
//
//        // 로그인 관련 POST 요청 (공개)
//        if ("POST".equals(method)) {
//            if (AccountApi.PUBLIC_POST_PATHS.contains(requestURI)) {
//                return true;
//            }
//        }
//
//        // GET 요청만 공개
//        if ("GET".equals(method)) {
//            if (EventApi.PUBLIC_GET_EXACT_PATHS.contains(requestURI)) {
//                return true;
//            }
//
//            // 쿼리 파라미터가 있는 경로
//            if (EventApi.PUBLIC_GET_PREFIX_PATHS.stream().anyMatch(requestURI::startsWith)) {
//                return true;
//            }
//
//            // 동적 경로 패턴
//            if (EventApi.PUBLIC_GET_PATTERN_PATHS.stream().anyMatch(pattern -> pathMatcher.match(pattern, requestURI))) {
//                return true;
//            }
//        }
//
//        return false; // 기타 모든 요청은 인증 필요
//    }
}