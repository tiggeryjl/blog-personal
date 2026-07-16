package com.blog.interceptor;

import com.blog.pojo.entity.LoginUser;
import com.blog.pojo.entity.SysRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import java.io.Serializable;
import java.util.List;

@Slf4j
@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    // 超级管理员角色编码
    private static final String SUPER_ADMIN_ROLE = "admin";

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        // 未登录直接拒绝
        if (!authentication.isAuthenticated() || authentication.getPrincipal() instanceof String) {
            return false;
        }

        // 获取登录用户信息
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        List<SysRole> roleList = loginUser.getSysUser().getRoleList();

        // 超级管理员直接放行所有权限
        if (roleList != null) {
            for (SysRole role : roleList) {
                // 从角色实体中获取角色编码比对
                if (SUPER_ADMIN_ROLE.equals(role.getRoleKey())) {
                    return true;
                }
            }
        }

        // 校验当前接口权限是否在用户权限集合中
        String needPerm = permission.toString();
        return loginUser.getPermissionList().contains(needPerm);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        return false;
    }
}
