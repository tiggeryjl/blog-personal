package com.blog.interceptor;

import com.blog.config.XssHttpServletRequestWrapper;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@WebFilter(filterName = "xssFilter", urlPatterns = "/*")
public class XssFilter implements Filter {

    // 富文本、文件上传等不需要XSS转义的接口白名单
    private static final List<String> WHITE_URL_LIST = Arrays.asList(
            "/article/add",
            "/article/update",
            "/upload"
    );

    // 放行逻辑
    private boolean isWhiteUrl(HttpServletRequest request) {
        String path = request.getServletPath();
        // 只要请求路径以白名单任意一个前缀开头，就放行
        for (String white : WHITE_URL_LIST) {
            if (path.startsWith(white)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) request;

        // 判断当前接口是否在白名单：在白名单则直接放行，不做XSS包装
        boolean whiteUrl = isWhiteUrl(httpReq);
        if (whiteUrl) {
            chain.doFilter(request, response);
        } else {
            // 非白名单接口执行XSS过滤
            chain.doFilter(new XssHttpServletRequestWrapper(httpReq), response);
        }
    }
}