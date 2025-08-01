package com.pyokemon.gateway_service.security.filter;

import com.pyokemon.gateway_service.security.jwt.authentication.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.function.ServerRequest;

import java.util.function.Function;

@Slf4j
class AuthenticationHeaderFilterFunction {
    public static Function<ServerRequest, ServerRequest> addHeader() {
        return request -> {
            log.info("=== AuthenticationHeaderFilterFunction.addHeader() 실행됨 ===");
            log.info("요청 URI: {}", request.uri());
            
            ServerRequest.Builder requestBuilder = ServerRequest.from(request);
            
            try {
                Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                log.info("Principal 타입: {}", principal != null ? principal.getClass().getSimpleName() : "null");
                log.info("Principal 값: {}", principal);
                
                if (principal instanceof UserPrincipal userPrincipal) {
                    log.info("UserPrincipal 인스턴스 확인됨");
                    
                    if (userPrincipal.getAccountId() != null) {
                        requestBuilder.header("X-Auth-AccountId", userPrincipal.getAccountId().toString());
                        log.info("X-Auth-AccountId 헤더 추가됨: {}", userPrincipal.getAccountId());
                    } else {
                        log.warn("UserPrincipal의 accountId가 null입니다");
                    }
                    
                    if (userPrincipal.getRole() != null) {
                        requestBuilder.header("X-Auth-Role", userPrincipal.getRole());
                        log.info("X-Auth-Role 헤더 추가됨: {}", userPrincipal.getRole());
                    } else {
                        log.warn("UserPrincipal의 role이 null입니다");
                    }
                } else {
                    log.warn("Principal이 UserPrincipal이 아닙니다. Principal: {}", principal);
                }
            } catch (Exception e) {
                log.error("사용자 인증 정보 추출 중 오류: {}", e.getMessage(), e);
            }
            
//            try {
//                String device = extractDeviceFromUserAgent(request);
//                requestBuilder.header("X-Client-Device", device);
//                log.info("X-Client-Device 헤더 추가됨: {}", device);
//            } catch (Exception e) {
//                log.warn("디바이스 정보 추출 실패: {}", e.getMessage());
//                requestBuilder.header("X-Client-Device", "unknown");
//            }
            return requestBuilder.build();
        };
    }
    
//    private static String extractDeviceFromUserAgent(ServerRequest request) {
//        String userAgent = request.headers().firstHeader("User-Agent");
//        if (userAgent == null || userAgent.isEmpty()) {
//            return "unknown";
//        }
//
//        userAgent = userAgent.toLowerCase();
//
//        if (userAgent.contains("mobile") || userAgent.contains("android") || userAgent.contains("iphone")) {
//            return "MOBILE";
//        } else if (userAgent.contains("tablet") || userAgent.contains("ipad")) {
//            return "TABLET";
//        } else if (userAgent.contains("postman") || userAgent.contains("curl")) {
//            return "API_CLIENT";
//        } else {
//            return "WEB";
//        }
//    }
}