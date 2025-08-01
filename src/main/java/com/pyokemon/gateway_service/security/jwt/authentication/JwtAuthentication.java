package com.pyokemon.gateway_service.security.jwt.authentication;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Getter
public class JwtAuthentication extends AbstractAuthenticationToken {
    private final String token;
    private final UserPrincipal principal;

    public JwtAuthentication(UserPrincipal principal, String token) {
        super(createAuthorities(principal));
        this.principal = principal;
        this.token = token;
        this.setDetails(principal);
        setAuthenticated(true);
    }

    public JwtAuthentication(UserPrincipal principal, String token,
                             Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.token = token;
        this.setDetails(principal);
        setAuthenticated(true);
    }

    /**
     * UserPrincipal의 role 정보를 기반으로 권한 생성
     */
    private static Collection<? extends GrantedAuthority> createAuthorities(UserPrincipal principal) {
        if (principal.getRole() == null) {
            return Collections.emptyList();
        }
        
        String role = principal.getRole().toUpperCase();
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }
        
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public String getCredentials() {
        return token;
    }

    @Override
    public UserPrincipal getPrincipal() {
        return principal;
    }
    
    /**
     * Account ID 반환
     */
    public Long getAccountId() {
        return principal.getAccountId();
    }
    
    /**
     * Role 반환
     */
    public String getRole() {
        return principal.getRole();
    }
    
    /**
     * 특정 역할 확인
     */
    public boolean hasRole(String role) {
        if (principal.getRole() == null) {
            return false;
        }
        return principal.getRole().equalsIgnoreCase(role) || 
               principal.getRole().equalsIgnoreCase("ROLE_" + role);
    }
}