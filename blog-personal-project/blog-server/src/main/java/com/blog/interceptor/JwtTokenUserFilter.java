package com.blog.interceptor;

import com.blog.constant.MessageConstant;
import com.blog.context.BaseContext;
import com.blog.pojo.entity.LoginUser;
import com.blog.properties.JwtProperties;
import com.blog.result.Result;
import com.blog.service.JwtService;
import com.blog.service.impl.UserDetailServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtTokenUserFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private UserDetailServiceImpl userDetailService;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getRequestURI();
        if (uri.endsWith("/login")
                || uri.endsWith("/refreshToken")
                || uri.endsWith("/register")
                || uri.equals("/error")
                || uri.equals("/favicon.ico")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 1. 从请求头获取 token
            String token = null;
            String bearer = request.getHeader(jwtProperties.getUserTokenName());
            if (bearer != null && bearer.startsWith("Bearer ")) {
                token = bearer.substring(7);
            } else {
                // 兼容旧前端 header: token
                token = request.getHeader("token");
            }

            // 2. 判空
            if (!StringUtils.hasText(token)) {
                // 没有token，放行交给security做未登录拦截
                filterChain.doFilter(request, response);
                return;
            }

            // 3. 校验 token 是否合法
            if (!jwtService.validateToken(token)) {
                filterChain.doFilter(request, response);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(objectMapper.writeValueAsString(Result.error(401, MessageConstant.LOGIN_EXPIRED_NOT_FOUND)));
                return;
            }

            // 只有当前没有认证信息时，才查询用户权限并存入Security上下文
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                // 根据username查询封装好权限的LoginUser
                LoginUser loginUser = (LoginUser) userDetailService.loadUserByUsername(jwtService.getUsername(token));
                // 用户已被删除/禁用
                if (loginUser == null) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write(objectMapper.writeValueAsString(Result.error(401, MessageConstant.ACCOUNT_NOT_FOUND)));
                    return;
                }
                BaseContext.setCurrentId(loginUser.getSysUser().getId());
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
                // 存入安全上下文
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

            filterChain.doFilter(request, response);
        } finally {
            // 用完清除，防止线程复用串数据
            BaseContext.removeCurrentId();
        }
    }
}
