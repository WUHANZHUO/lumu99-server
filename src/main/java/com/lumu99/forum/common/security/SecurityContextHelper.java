package com.lumu99.forum.common.security;

import com.lumu99.forum.common.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityContextHelper {

    private SecurityContextHelper() {
    }

    public static String currentUserUuid() {
        Authentication auth = getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH_401_UNAUTHORIZED", "Unauthorized");
        }
        return String.valueOf(auth.getPrincipal());
    }

    public static String currentUserRole() {
        Authentication auth = getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return "ANONYMOUS";
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5))
                .findFirst()
                .orElse("USER");
    }

    public static boolean isAdmin() {
        return "ADMIN".equals(currentUserRole());
    }

    public static boolean isGuest() {
        Authentication auth = getAuthentication();
        return auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken;
    }

    private static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
