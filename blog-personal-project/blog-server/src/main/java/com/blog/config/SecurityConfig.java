package com.blog.config;

import com.blog.handler.JwtAccessDeniedHandler;
import com.blog.handler.JwtAuthenticationEntryPoint;
import com.blog.interceptor.CustomPermissionEvaluator;
import com.blog.interceptor.JwtTokenUserFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@Configuration
@EnableWebSecurity
// 开启方法级权限校验
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {


    @Autowired
    private JwtTokenUserFilter jwtTokenUserFilter;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtAccessDeniedHandler jwtAccessDeniedHandler;

    // 密码加密器
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 认证管理器（登录校验需要）
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // 核心安全过滤链配置
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("开始配置SpringSecurity安全过滤链，注册JWT过滤器");
        http
                // 关闭csrf
                .csrf(csrf -> csrf.disable())
                // 无状态，不使用session
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 权限配置
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/user/user/login",
                                "/user/user/register",
                                "/user/user/refreshToken",
                                "/user/user/logout",
                                "/admin/admin/login",
                                "/admin/admin/refreshToken",
                                "/admin/admin/logout",
                                "/link/apply",
                                "/link/list",
                                "/link/applications",
                                "/AiChat/ai/stream-chat",
                                "/ws/admin/notice",
                                "/error",
                                "/favicon.ico").permitAll() // 放行登录
                        .anyRequest().authenticated() // 其他接口必须认证
                )
                // 自定义401未登录、403权限不足异常返回
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                );

        // 将JWT过滤器加到用户名密码认证过滤器之前执行
        http.addFilterBefore(jwtTokenUserFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

//    @Bean
//    public DefaultMethodSecurityExpressionHandler methodSecurityExpressionHandler(CustomPermissionEvaluator permissionEvaluator) {
//        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
//        handler.setPermissionEvaluator(permissionEvaluator);
//        return handler;
//    }
    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler(CustomPermissionEvaluator customPermissionEvaluator) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        // 绑定自定义权限校验器，hasPermission 才能识别单参数写法
        handler.setPermissionEvaluator(customPermissionEvaluator);
        return handler;
    }

}
