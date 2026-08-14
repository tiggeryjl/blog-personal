package com.blog.service;

import com.blog.pojo.dto.RoleDTO;
import com.blog.pojo.dto.RoleMenuAssignDTO;
import com.blog.pojo.dto.RolePageQueryDTO;
import com.blog.pojo.vo.RoleMenuTreeVO;
import com.blog.pojo.vo.SysRoleVo;
import com.blog.result.PageResult;

import java.util.List;

public interface SysRoleService {

    /**
     * 分页查询角色
     * @param rolePageQueryDTO
     * @return
     */
    PageResult pageQuery(RolePageQueryDTO rolePageQueryDTO);

    /**
     * 新增角色
     * @param roleDTO
     */
    void add(RoleDTO roleDTO);

    /**
     * 修改角色
     * @param roleDTO
     */
    void update(RoleDTO roleDTO);

    /**
     * 逻辑删除角色
     * @param id
     */
    void logicDelete(Long id);

    /**
     * 查询逻辑删除的角色列表
     * @return
     */
    List<SysRoleVo> getLogicDelete();

    /**
     * 恢复角色
     * @param id
     */
    void recover(Long id);

    /**
     * 彻底删除角色
     * @param id
     */
    void delete(Long id);

    /**
     * 获取角色权限树
     * @param id
     * @return
     */
    RoleMenuTreeVO getRoleMenuTree(Long id);

    /**
     * 分配权限
     * @param roleMenuAssignDTO
     */
    void assignRoleMenu(RoleMenuAssignDTO roleMenuAssignDTO);
}
