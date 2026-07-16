package com.blog.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.web.util.HtmlUtils;

public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    // 过滤get参数
    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        return stripXSS(value);
    }

    // 过滤get数组参数
    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) {
            return null;
        }
        String[] newValues = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            newValues[i] = stripXSS(values[i]);
        }
        return newValues;
    }

    // 过滤header
    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        return stripXSS(value);
    }

    /**
     * XSS核心过滤方法：HTML转义，防止脚本注入
     */
    private String stripXSS(String value) {
        if (value != null) {
            // 1. HTML特殊字符转义：< > " ' & 转为实体字符
            value = HtmlUtils.htmlEscape(value);

            // 可选：过滤危险脚本标签（script、iframe、onload、onclick等）
            value = value.replaceAll("<script.*?</script>", "");
            value = value.replaceAll("on\\w+=\"[^\"]*\"", "");
            value = value.replaceAll("on\\w+='[^']*'", "");
            value = value.replaceAll("<iframe.*?</iframe>", "");
        }
        return value;
    }
}
