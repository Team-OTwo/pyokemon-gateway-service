package com.pyokemon.gateway_service.common.filter;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 내부 마이크로서비스로 전달되는 요청에 traceId와 spanId를 헤더에 추가하는 필터
 */
@Slf4j
@Component
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
            
            // 표준 W3C 헤더 추가
            response.addHeader("traceparent", "00-" + traceId + "-" + spanId + "-01");
            
            // 커스텀 헤더 추가 (서비스간 호환성을 위해)
            response.addHeader("X-B3-TraceId", traceId);
            response.addHeader("X-B3-SpanId", spanId);
            
            log.debug("Trace headers added: traceId={}, spanId={}", traceId, spanId);
        }
        
        filterChain.doFilter(request, response);
    }
} 