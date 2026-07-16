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
public class RoleMenuTreeVO implements Serializable {

    /**
     * 权限树
     */
    private List<MenuTreeVO> menuTreeVOList;

    /**
     * 父菜单id 0代表顶级菜单
     */
    private List<Long> checkedKeys;


}
