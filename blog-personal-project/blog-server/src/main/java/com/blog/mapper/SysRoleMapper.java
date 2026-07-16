package com.blog.mapper;

import com.blog.pojo.dto.RolePageQueryDTO;
import com.blog.pojo.entity.SysRole;
import com.blog.pojo.vo.RoleSelectVO;
import com.blog.pojo.vo.SysRoleVo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysRoleMapper {

    /**
     * 根据角色id查找角色
     * @param roleIds
     * @return
     */
    List<SysRole> selectRolesByRoleIds(@Param("roleIds") List<Long> roleIds);

    /**
     * 分页查询角色
     * @param rolePageQueryDTO
     * @return
     */
    List<SysRoleVo> pageQuery(RolePageQueryDTO rolePageQueryDTO);

    /**
     * 新增角色
     * @param sysRole
     */
    void add(SysRole sysRole);

    /**
     * 修改角色
     * @param sysRole
     */
    void update(SysRole sysRole);

    /**
     * 彻底删除角色
     * @param id
     */
    @Delete("delete from sys_role where id = #{id}")
    void delete(Long id);

    @Select("select * from sys_role where id =#{roleId}")
    SysRole selectById(Long roleId);

    /**
     * 获取用户角色集合
     * @return
     */
    List<RoleSelectVO> getRoleList();
}
