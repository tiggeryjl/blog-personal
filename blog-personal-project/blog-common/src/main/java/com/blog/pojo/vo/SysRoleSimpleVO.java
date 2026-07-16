package com.blog.pojo.vo;

import lombok.Data;

@Data
public class SysRoleSimpleVO {
    private Long id;
    // 角色名称
    private String roleName;
    // 角色标识
    private String roleKey;
}
