package com.pyokemon.gateway_service.common.filter;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 내부 마이크로서비스로 전달되는 요청에 traceId와 spanId를 헤더에 추가하는 필터
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)  // TracingFilter 다음으로 실행
@RequiredArgsConstructor
public class TraceIdAddingFilter extends OncePerRequestFilter {

    private final Tracer tracer;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        
        Span currentSpan = tracer.currentSpan();
        
        if (currentSpan != null) {
            String traceId = currentSpan.context().traceId();
            String spanId = currentSpan.context().spanId();
            
            // 표준 W3C 헤더 추가 (서비스 간 전파)
            response.addHeader("traceparent", "00-" + traceId + "-" + spanId + "-01");
            
            // 커스텀 헤더 추가 (서비스간 호환성을 위해)
            response.addHeader("X-B3-TraceId", traceId);
            response.addHeader("X-B3-SpanId", spanId);
            
            // 클라이언트용 응답 헤더 추가
            response.addHeader("X-Trace-Id", traceId);
            
            log.debug("Trace headers added: traceId={}, spanId={}", traceId, spanId);
        }
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            // 필터 체인이 모두 실행된 후에도 X-Trace-Id 헤더가 확실히 포함되어 있도록 한 번 더 확인
            if (currentSpan != null && !response.containsHeader("X-Trace-Id")) {
                response.addHeader("X-Trace-Id", currentSpan.context().traceId());
            }
        }
    }
} 