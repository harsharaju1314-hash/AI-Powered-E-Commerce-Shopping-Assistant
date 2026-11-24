package com.ecommerce.assistant.security;

import com.ecommerce.assistant.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class OwnershipValidator {

    public Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal)) {
            throw new UnauthorizedException("User is not authenticated");
        }
        return ((UserPrincipal) auth.getPrincipal()).getId();
    }

    public void validateOwnership(Long resourceOwnerId) {
        Long currentUserId = getCurrentUserId();
        if (!currentUserId.equals(resourceOwnerId)) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (!isAdmin) {
                throw new UnauthorizedException("Access denied: You do not own this resource");
            }
        }
    }
}
