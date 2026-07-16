package com.blog.service.impl;

import com.blog.constant.MessageConstant;
import com.blog.mapper.SysMenuMapper;
import com.blog.mapper.SysUserMapper;
import com.blog.pojo.entity.LoginUser;
import com.blog.pojo.entity.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailServiceImpl implements UserDetailsService {

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
        List<String> perms = sysMenuMapper.selectPermsByUserId(user.getId());
        return new LoginUser(user, perms);
    }

}
