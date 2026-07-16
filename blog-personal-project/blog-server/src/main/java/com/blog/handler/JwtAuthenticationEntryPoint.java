package com.blog.handler;

import com.alibaba.fastjson.JSON;
import com.blog.constant.MessageConstant;
import com.blog.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 401 自定义异常处理器
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        Result<Object> result = Result.error(401, MessageConstant.TOKEN_EXPIRED);
        response.getWriter().write(JSON.toJSONString(result));
    }
}
