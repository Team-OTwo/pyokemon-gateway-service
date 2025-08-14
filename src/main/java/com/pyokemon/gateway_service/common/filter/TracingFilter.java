package com.pyokemon.gateway_service.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.Span;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 모든 요청에 대해 traceId를 생성하고 MDC에 설정하는 필터
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TracingFilter extends OncePerRequestFilter {

    private final Tracer tracer;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // 현재 활성화된 span 정보 가져오기
        Span currentSpan = tracer.currentSpan();
        
        // 요청 시작시 로그 기록
        if (currentSpan != null) {
            String traceId = currentSpan.context().traceId();
            String spanId = currentSpan.context().spanId();
            
            // 헤더에 traceId 추가 (다른 서비스로 전파할 때 사용)
            response.addHeader("X-Trace-Id", traceId);
            
            log.info("요청 시작: [trace_id={}] [span_id={}] {} {}", 
                    traceId, spanId, request.getMethod(), request.getRequestURI());
        } else {
            log.info("요청 시작: [trace 없음] {} {}", request.getMethod(), request.getRequestURI());
        }
        
        try {
            // 다음 필터 실행
            filterChain.doFilter(request, response);
        } finally {
            // 요청 종료시 로그 기록
            if (currentSpan != null) {
                log.info("요청 종료: [trace_id={}] [span_id={}] {} {} - {}",
                        currentSpan.context().traceId(),
                        currentSpan.context().spanId(),
                        request.getMethod(),
                        request.getRequestURI(),
                        response.getStatus());
            } else {
                log.info("요청 종료: [trace 없음] {} {} - {}", 
                        request.getMethod(), request.getRequestURI(), response.getStatus());
            }
        }
    }
} 