package com.example.back_end.utils;

import com.example.back_end.common.BizException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthUtils {
    public static Long getCurrentUserId() {
        // 1. 从线程绑定的上下文中获取认证对象
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long) {
            return (Long) auth.getPrincipal();
        }
        throw new BizException(401, "未登录");
    }
}
