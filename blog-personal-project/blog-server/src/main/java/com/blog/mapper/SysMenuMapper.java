package com.blog.mapper;

import com.blog.pojo.entity.SysMenu;
import com.blog.pojo.vo.SysMenuVo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Mapper
public interface SysMenuMapper {

    /**
     * 根据角色id集合查询权限菜单
     * @param roleIds
     * @return
     */
    List<SysMenu> selectMenuListByRoleIds(@Param("roleIds") List<Long> roleIds);

    /**
     * 根据用户id获取所有权限标识
     * @param userId
     * @return
     */
    List<String> selectPermsByUserId(Long userId);

    /**
     * 根据状态获取权限集合
     * @param status
     * @return
     */
    List<SysMenu> selectAllMenuList(Integer status,Integer deleteFlag);

    /**
     * 获取所有状态权限集合
     * @return
     */
    List<SysMenu> selectMenuList();

    /**
     * 根据菜单id查询权限
     * @param id
     * @return
     */
    SysMenu selectById(Long id);

    /**
     * 新增权限
     */
    void add(SysMenu menu);

    /**
     * 修改权限
     */
    void update(SysMenu menu);

    /**
     * 批量修改
     * @param id
     * @param status
     */
    void updateChildMenuStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 逻辑删除权限
     * @param menu
     * @param allMenuIds
     */
    void batchUpdateByIds(@Param("menu") SysMenu menu,@Param("allMenuIds") List<Long> allMenuIds);

    /**
     * 彻底删除角色
     * @param id
     */
    void delete(Long id);

    /**
     * 当前菜单及其所有子菜单ID集合
     * @param id
     * @return
     */
    List<Long> getAllChildMenuIds(Long id);

    List<SysMenu> selectBatchIds(List<Long> menuIds);

    /**
     * 根据角色id查询所有父菜单权限
     * @param menuId
     * @return
     */
    List<SysMenu> getAllParentMenuIds(@Param("menuId") Long menuId);

    Boolean existsProtectedAncestor(@Param("menuId") Long menuId);
    int countProtectedInIds(@Param("ids") List<Long> ids);
}
