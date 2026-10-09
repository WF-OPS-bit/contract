package com.ar.contractreview.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;

public class SecurityUtils {

    /** 角色权限的前缀，见 security/service/impl/UserService#wrapRoleAndPerm */
    private static final String ROLE_PREFIX = "ROLE_";

    public static String currentUsername() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return a == null ? null : a.getName();
    }

    public static Long currentUserId() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return a == null || a.getCredentials() == null ? null : (Long) a.getCredentials();
    }

    /**
     * 取当前登录用户的角色标识。
     * <p>
     * 登录时 UserService 会把角色封装成 {@code ROLE_xxx} 放进权限列表
     * （其余元素是菜单权限，如 {@code contract:list}），这里取出角色那一条并去掉前缀。
     * </p>
     * <p>
     * 之所以需要它：contract_comment.user_role 是 NOT NULL 字段，
     * 而前端并不会传角色，必须由后端从登录态补齐。
     * </p>
     *
     * @return 角色标识；取不到时返回空串（保证写入非空字段不报错）
     */
    public static String currentUserRole() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || a.getAuthorities() == null) return "";
        Collection<? extends GrantedAuthority> authorities = a.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            String value = authority.getAuthority();
            if (value != null && value.startsWith(ROLE_PREFIX)) {
                return value.substring(ROLE_PREFIX.length());
            }
        }
        return "";
    }
}
