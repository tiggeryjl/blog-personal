package com.blog.permission;

import com.blog.mapper.SysUserRoleMapper;
import com.blog.pojo.entity.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * 检查用户当前是否具有访问仅限管理员的频道的权限
 */
@Component("adminAccess")
public class AdminAccessChecker {

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    public boolean hasAccess(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof LoginUser loginUser)
                || loginUser.getSysUser() == null) {
            return false;
        }
        return hasAccess(loginUser.getSysUser().getId());
    }

    public boolean hasAccess(Long userId) {
        return userId != null && sysUserRoleMapper.hasActiveAdminAccess(userId);
    }
}
