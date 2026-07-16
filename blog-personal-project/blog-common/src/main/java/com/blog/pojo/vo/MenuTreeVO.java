package com.blog.pojo.vo;

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
public class MenuTreeVO implements Serializable {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 父菜单id 0代表顶级菜单
     */
    private Long parentId;

    /**
     * 权限名
     */
    private String menuName;

    /**
     * 权限标识
     */
    private String perms;

    /**
     * 权限类型
     */
    private Integer menuType;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 子权限树
     */
    private List<MenuTreeVO> children;
}
