package com.example.back_end.config;

import com.example.back_end.service.ITokenBlacklistService;
import com.example.back_end.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final ITokenBlacklistService tokenBlacklistService;

    public JwtAuthFilter(JwtUtils jwtUtils, ITokenBlacklistService tokenBlacklistService) {
        this.jwtUtils = jwtUtils;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // 每个请求先经此过滤器：校验 Bearer token + 黑名单 → 写入 SecurityContext，后续接口从上下文取 userId
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            Claims claims = jwtUtils.parseClaimsIfValid(token);
            if (claims != null) {
                boolean blacklisted = false;
                try {
                    blacklisted = tokenBlacklistService.isBlacklisted(token);
                } catch (Exception e) {
                    // Redis 故障时 fail-open：不阻塞请求，靠 token 自然过期兜底
                    log.error("黑名单查询失败，按未拉黑处理 path={}", request.getRequestURI(), e);
                }
                if (!blacklisted) {
                    Long userId = claims.get("userId", Long.class);
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } else {
                log.debug("token 校验失败，跳过认证 path={}", request.getRequestURI());
            }
        }
        chain.doFilter(request, response);
    }
}
