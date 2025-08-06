package com.pyokemon.gateway_service.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        
        long startTime = System.currentTimeMillis();
        
        // 요청 정보 로깅
        String requestUri = requestWrapper.getRequestURI();
        String requestMethod = requestWrapper.getMethod();
        
        // 요청 헤더 추출
        Map<String, String> requestHeaders = getRequestHeaders(requestWrapper);
        
        log.info("[REQUEST] {} {} 시작", requestMethod, requestUri);
        log.info("[REQUEST HEADERS] {}", requestHeaders);
        
        // 필터 체인 실행
        filterChain.doFilter(requestWrapper, responseWrapper);
        
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        
        // 요청 본문 로깅
        String requestBody = new String(requestWrapper.getContentAsByteArray());
        if (!requestBody.isEmpty()) {
            log.info("[REQUEST BODY] {}", requestBody);
        }
        
        // 응답 정보 로깅
        int statusCode = responseWrapper.getStatus();
        
        // 응답 헤더 추출
        Map<String, String> responseHeaders = getResponseHeaders(responseWrapper);
        
        // 응답 본문 로깅 
        String responseBody = new String(responseWrapper.getContentAsByteArray());
        
        log.info("[RESPONSE] {} {} => {} (실행 시간: {}ms)", requestMethod, requestUri, statusCode, executionTime);
        log.info("[RESPONSE HEADERS] {}", responseHeaders);
        
        if (!responseBody.isEmpty()) {
            log.info("[RESPONSE BODY] {}", responseBody);
        }
        
        // 중요: 응답 내용을 다시 복사해야 클라이언트에게 정상적으로 전송됨
        responseWrapper.copyBodyToResponse();
    }
    
    private Map<String, String> getRequestHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            headers.put(headerName, headerValue);
        }
        
        return headers;
    }
    
    private Map<String, String> getResponseHeaders(HttpServletResponse response) {
        Map<String, String> headers = new HashMap<>();
        Collection<String> headerNames = response.getHeaderNames();
        
        for (String headerName : headerNames) {
            String headerValue = response.getHeader(headerName);
            headers.put(headerName, headerValue);
        }
        
        return headers;
    }
} 