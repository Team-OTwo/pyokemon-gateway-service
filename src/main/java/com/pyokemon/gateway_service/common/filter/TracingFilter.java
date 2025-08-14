package com.pyokemon.gateway_service.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.Span;
import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)  
@RequiredArgsConstructor
public class TracingFilter extends OncePerRequestFilter {

    private final Tracer tracer;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // 현재 활성화된 span 정보 가져오기
        Span currentSpan = tracer.currentSpan();
        
        if (currentSpan != null) {
            String traceId = currentSpan.context().traceId();
            
            response.addHeader("X-Trace-Id", traceId);
        } 

        try {
            filterChain.doFilter(request, response);
        } finally {
            if (currentSpan != null) {
                String traceId = currentSpan.context().traceId();
                
                if (!response.containsHeader("X-Trace-Id")) {
                    response.addHeader("X-Trace-Id", traceId);
                }
                
            }
        }
    }
} 