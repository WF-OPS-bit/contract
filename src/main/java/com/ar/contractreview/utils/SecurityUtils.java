package com.ar.contractreview.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    public static String currentUsername() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return a == null ? null : a.getName();
    }
    public static Long currentUserId() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return a == null || a.getCredentials() == null ? null : (Long) a.getCredentials();
    }
}
