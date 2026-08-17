package com.blog.service.impl;

import com.blog.constant.MessageConstant;
import com.blog.mapper.SysMenuMapper;
import com.blog.mapper.SysUserMapper;
import com.blog.pojo.entity.LoginUser;
import com.blog.pojo.entity.SysRole;
import com.blog.pojo.entity.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailServiceImpl implements UserDetailsService {

    /**
     * 超级管理员角色标识（对应 sys_role.role_key = 'admin'）
     */
    private static final String SUPER_ADMIN_ROLE_KEY = "admin";

    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private SysMenuMapper sysMenuMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = sysUserMapper.selectByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 装载用户角色，供权限校验器判断超管（roleList 之前为空导致超管放行从未生效）
        List<SysRole> roleList = sysUserMapper.selectRoleListByUserId(user.getId());
        user.setRoleList(roleList);

        // 超级管理员直接拥有全部菜单权限，无需依赖 sys_role_menu 中间表绑定
        boolean isSuperAdmin = roleList != null && roleList.stream()
                .anyMatch(role -> SUPER_ADMIN_ROLE_KEY.equals(role.getRoleKey()));
        List<String> perms = isSuperAdmin
                ? sysMenuMapper.selectAllPerms()
                : sysMenuMapper.selectPermsByUserId(user.getId());

        return new LoginUser(user, perms);
    }

}
