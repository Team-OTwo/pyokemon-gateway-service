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
            final Claims claims = Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String subject = claims.getSubject();
            String role = claims.get("role", String.class);

            if (subject == null || subject.trim().isEmpty()) {
                log.warn("Invalid JWT: Subject is null or empty.");
                return null;
            }

            Long accountId;
            try {
                accountId = Long.parseLong(subject);
            } catch (NumberFormatException e) {
                log.warn("Invalid JWT: Subject is not a valid Long.");
                return null;
            }

            UserPrincipal principal = new UserPrincipal(accountId, role);
            return new JwtAuthentication(principal, token);

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("Expired JWT token: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Invalid JWT token processing error", e);
            return null;
        }
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
