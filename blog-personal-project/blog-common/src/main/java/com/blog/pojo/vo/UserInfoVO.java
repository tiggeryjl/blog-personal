package com.blog.pojo.vo;

import com.blog.pojo.entity.SysMenu;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoVO implements Serializable {

    private UserSimpleVO user;

    // 角色标识数组
    private List<String> roles;

    // 权限标识符数组
    private List<String> permissions;

    // 前端动态路由菜单
    private List<SysMenu> routers;
}
