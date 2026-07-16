package com.blog.mapper;

import com.blog.pojo.entity.SysRoleMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysRoleMenuMapper {

    int batchInsert(@Param("roleMenuList") List<SysRoleMenu> roleMenuList);

    // 根据角色id删除
    int deleteByRoleId(Long roleId);

    // 根据权限id删除
    int deleteByMenuId(List<Long> menuIds);

    /**
     * 根据角色id获取权限id
     * @param id
     * @return
     */
    @Select("select menu_id from sys_role_menu where role_id = #{id}")
    List<Long> getMenuByRoleId(Long id);

    /**
     * 给指定角色(超管)绑定菜单权限
     * @param roleMenu
     */
    void addMenu(SysRoleMenu roleMenu);
}
