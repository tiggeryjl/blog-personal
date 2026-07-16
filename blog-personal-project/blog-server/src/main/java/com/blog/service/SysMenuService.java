package com.blog.service;


import com.blog.pojo.dto.MenuDTO;
import com.blog.pojo.vo.SysMenuVo;

import java.util.List;

public interface SysMenuService {

    /**
     * 获取权限树
     * @return
     */
    List<SysMenuVo> getMenuTree();

    /**
     * 新增权限
     * @param menuDTO
     */
    void add(MenuDTO menuDTO);

    /**
     * 修改角色
     * @param menuDTO
     */
    void update(MenuDTO menuDTO);

    /**
     * 逻辑删除权限
     * @param id
     */
    void logicDelete(Long id);

    /**
     * 彻底删除权限
     * @param id
     */
    void delete(Long id);

    /**
     * 查询逻辑删除的权限树
     * @return
     */
    List<SysMenuVo> getdeleteMenuTree();

    /**
     * 恢复删除权限
     * @param id
     */
    void recover(Long id);
}
