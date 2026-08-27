package com.blog.permission;

import com.blog.exception.CustomException;
import com.blog.interceptor.CustomPermissionEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 报表模块权限校验（供 @PreAuthorize SpEL 调用）
 */
@Component("reportPermission")
public class ReportPermission {

    @Autowired
    private CustomPermissionEvaluator permissionEvaluator;

    /**
     * 校验当前用户是否有对应报表模块的查看权限
     *
     * @param module works/comment/user/link
     */
    public boolean check(String module) {
        String permission = resolvePermission(module);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return permissionEvaluator.hasPermission(authentication, null, permission);
    }

    /**
     * 模块转权限标识
     */
    private String resolvePermission(String module) {
        switch (module) {
            case "works":
                return "sys:report:works:list";
            case "comment":
                return "sys:report:comment:list";
            case "user":
                return "sys:report:user:list";
            case "link":
                return "sys:report:link:list";
            default:
                throw new CustomException("未知的报表模块:" + module);
        }
    }
}
