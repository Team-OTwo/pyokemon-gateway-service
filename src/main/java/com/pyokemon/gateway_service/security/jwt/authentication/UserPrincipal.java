package com.pyokemon.gateway_service.security.jwt.authentication;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.security.Principal;
import java.util.Objects;

@Getter
@RequiredArgsConstructor
public class UserPrincipal implements Principal {
    private final Long accountId;
    private final String role;

    public boolean hasName() {
        return accountId != null;
    }

    public boolean hasMandatory() {
        return accountId != null;
    }
    
    /**
     * Account ID가 존재하는지 확인
     */
    public boolean hasAccountId() {
        return accountId != null;
    }
    
    /**
     * Role이 존재하는지 확인
     */
    public boolean hasRole() {
        return role != null && !role.trim().isEmpty();
    }
    
    
    /**
     * 특정 역할 확인
     */
    public boolean hasRole(String targetRole) {
        if (!hasRole()) {
            return false;
        }
        return role.equalsIgnoreCase(targetRole) || 
               role.equalsIgnoreCase("ROLE_" + targetRole);
    }
    
    /**
     * 유효한 사용자인지 확인
     */
    public boolean isValid() {
        return hasAccountId() && hasRole();
    }

    @Override
    public String toString() {
        return String.format("UserPrincipal{accountId=%d, role='%s'}", 
                           accountId, role);
    }

    @Override
    public String getName() {
        return accountId != null ? accountId.toString() : null;
    }

    @Override
    public boolean equals(Object another) {
        if (this == another) return true;
        if (another == null) return false;
        if (!getClass().isAssignableFrom(another.getClass())) return false;
        UserPrincipal principal = (UserPrincipal) another;
        if (!Objects.equals(accountId, principal.accountId)) {
            return false;
        }
        if (!Objects.equals(role, principal.role)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = accountId != null ? accountId.hashCode() : 0;
        result = 31 * result + (role != null ? role.hashCode() : 0);
        return result;
    }
    
    /**
     * UserPrincipal 빌더 클래스
     */
    public static class Builder {
        private Long accountId;
        private String role;
        
        public Builder accountId(Long accountId) {
            this.accountId = accountId;
            return this;
        }
        
        public Builder role(String role) {
            this.role = role;
            return this;
        }
        
        public UserPrincipal build() {
            if (accountId == null) {
                throw new IllegalArgumentException("accountId cannot be null");
            }
            return new UserPrincipal(accountId, role);
        }
    }
    
    /**
     * 빌더 인스턴스 생성
     */
    public static Builder builder() {
        return new Builder();
    }
}