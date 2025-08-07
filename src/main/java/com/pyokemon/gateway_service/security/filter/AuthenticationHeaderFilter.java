package com.pyokemon.gateway_service.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import com.pyokemon.gateway_service.security.jwt.authentication.UserPrincipal;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthenticationHeaderFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.info("=== AuthenticationHeaderFilter 실행됨 ===");
        log.info("요청 URI: {}", request.getRequestURI());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            Object principal = authentication.getPrincipal();
            log.info("Principal 타입: {}", principal != null ? principal.getClass().getSimpleName() : "null");
            log.info("Principal 값: {}", principal);

            if (principal instanceof UserPrincipal userPrincipal) {
                log.info("UserPrincipal 인스턴스 확인됨");
                if (userPrincipal.getAccountId() != null) {
                    response.addHeader("X-Auth-AccountId", userPrincipal.getAccountId().toString());
                    log.info("X-Auth-AccountId 헤더 추가됨: {}", userPrincipal.getAccountId());
                } else {
                    log.warn("UserPrincipal의 accountId가 null입니다");
                }
                if (userPrincipal.getRole() != null) {
                    response.addHeader("X-Auth-Role", userPrincipal.getRole());
                    log.info("X-Auth-Role 헤더 추가됨: {}", userPrincipal.getRole());
                } else {
                    log.warn("UserPrincipal의 role이 null입니다");
                }
            } else {
                log.warn("Principal이 UserPrincipal이 아닙니다. Principal: {}", principal);
            }
        } else {
            log.warn("인증 정보가 없거나 익명 사용자입니다. 헤더 추가를 건너뜁니다.");
        }

        filterChain.doFilter(request, response);
    }
}