package com.pyokemon.gateway_service.security.jwt.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
@ConfigurationProperties(value = "jwt", ignoreUnknownFields = true)
@Getter
@Setter
public class JwtConfigProperties {
    
    private String header = "Authorization";
    private String secretKey;
    private long expiresIn = 86400000;
    private long mobileExpiresIn = 86400000;
    
    /**
     * 설정 유효성 검증
     */
    @PostConstruct
    public void validate() {
        if (secretKey == null || secretKey.trim().isEmpty()) {
            throw new IllegalArgumentException("secretKey는 필수 입력 값입니다.");
        }
        
        if (header == null || header.trim().isEmpty()) {
            throw new IllegalArgumentException("header는 필수 입력 값입니다.");
        }
        
        if (expiresIn <= 0) {
            throw new IllegalArgumentException("expiresIn은 0보다 커야 합니다.");
        }
        
        if (mobileExpiresIn <= 0) {
            throw new IllegalArgumentException("mobileExpiresIn은 0보다 커야 합니다.");
        }
    }
    
    /**
     * 설정 정보 로깅 (보안상 민감한 정보는 마스킹)
     */
    public void logConfiguration() {
        System.out.println("JWT Configuration:");
        System.out.println("  Header: " + header);
        System.out.println("  Secret Key: " + maskSecretKey(secretKey));
        System.out.println("  Expires In: " + expiresIn + "ms");
        System.out.println("  Mobile Expires In: " + mobileExpiresIn + "ms");
    }
    
    /**
     * Secret Key 마스킹 (로그 출력용)
     */
    private String maskSecretKey(String secretKey) {
        if (secretKey == null || secretKey.length() <= 8) {
            return "***";
        }
        return secretKey.substring(0, 4) + "..." + secretKey.substring(secretKey.length() - 4);
    }
    
    /**
     * 만료 시간을 초 단위로 반환
     */
    public long getExpiresInSeconds() {
        return expiresIn / 1000;
    }
    
    /**
     * 모바일 만료 시간을 초 단위로 반환
     */
    public long getMobileExpiresInSeconds() {
        return mobileExpiresIn / 1000;
    }
}