package com.blog.mapper;

import com.blog.pojo.entity.SysUserRole;
import com.blog.pojo.vo.SysRoleSimpleVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysUserRoleMapper {

    // 根据用户id批量删除角色关联
    int deleteByUserIds(List<Long> ids);

    // 根据角色id删除用户角色关联
    int deleteByRoleId(Long roleId);

    // 批量新增用户角色
    int batchInsert(@Param("list") List<SysUserRole> userRoleList);

    /**
     * 根据用户id查询角色列表（id、roleName、roleKey）
     */
    List<SysRoleSimpleVO> selectRoleByUserId(@Param("userId") Long userId);

    /**
     * 根据用户id 判断该用户是否拥有指定角色
     */
    boolean hasRole(@Param("userId") Long userId, @Param("roleKey") String roleKey);
}
