package com.blog.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SysMenu implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    //父菜单id
    private Long parentId;

    //菜单名称 0代表顶级菜单
    private String menuName;

    //前端路由访问地址
    private String path;

    //前端页面组件路径
    private String component;

    //权限标识符，接口、按钮鉴权使用
    private String perms;

    //菜单类型：0=目录 1=页面菜单 2=按钮权限
    private Integer menuType;

    //菜单图标
    private String icon;

    //菜单排序
    private Integer sort;

    //状态 0=正常 1=禁用 2=注销
    private Integer status;

    // 逻辑删除 0=正常 1=删除
    private Integer deleteFlag;

    //创建时间
    private LocalDateTime createTime;

    //修改时间
    private LocalDateTime updateTime;

    // 树形菜单子节点
    private List<SysMenu> children;
}
