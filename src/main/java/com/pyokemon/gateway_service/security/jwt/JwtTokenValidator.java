package com.pyokemon.gateway_service.security.jwt;

import com.pyokemon.gateway_service.security.jwt.authentication.JwtAuthentication;
import com.pyokemon.gateway_service.security.jwt.authentication.UserPrincipal;
import com.pyokemon.gateway_service.security.jwt.props.JwtConfigProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenValidator {
    private final JwtConfigProperties configProperties;
    private volatile SecretKey secretKey;

    private SecretKey getSecretKey() {
        if (secretKey == null) {
            synchronized (this) {
                if (secretKey == null) {
                    secretKey = Keys.hmacShaKeyFor(configProperties.getSecretKey().getBytes());
                }
            }
        }
        return secretKey;
    }

    public JwtAuthentication validateToken(String token) {
        try {
            final Claims claims = this.verifyAndGetClaims(token);
            if (claims == null) {
                return null;
            }
            
            Date expirationDate = claims.getExpiration();
            if (expirationDate == null || expirationDate.before(new Date())) {
                return null;
            }
            
            String subject = claims.getSubject();
            String role = claims.get("role", String.class);
            
            if (subject == null || subject.trim().isEmpty()) {
                return null;
            }
            
            Long accountId;
            try {
                accountId = Long.parseLong(subject);
            } catch (NumberFormatException e) {
                return null;
            }
            
            UserPrincipal principal = new UserPrincipal(accountId, role);
            
            JwtAuthentication authentication = new JwtAuthentication(principal, token);
            
            return authentication;
            
        } catch (Exception e) {
            return null;
        }
    }

    private Claims verifyAndGetClaims(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            claims = null;
        }
        return claims;
    }

    public String getToken(HttpServletRequest request) {
        String authHeader = getAuthHeaderFromHeader(request);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return token;
        }
        return null;
    }

    private String getAuthHeaderFromHeader(HttpServletRequest request) {
        return request.getHeader(configProperties.getHeader());
    }
}
