package com.procrastination.killer.common;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 当前登录用户工具
 * 从 SecurityContext 中提取当前请求的用户ID
 */
@Component
public class CurrentUserHelper {

    /**
     * 获取当前登录用户ID
     * 若未登录则抛出异常
     */
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("用户未登录");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Long userId) {
            return userId;
        }
        throw new RuntimeException("无法获取当前用户信息");
    }
}
