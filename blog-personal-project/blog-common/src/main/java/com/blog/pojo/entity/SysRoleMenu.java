package com.blog.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SysRoleMenu implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    //角色id
    private Long roleId;

    //菜单权限id
    private Long menuId;

    //创建时间
    private LocalDateTime createTime;
}
