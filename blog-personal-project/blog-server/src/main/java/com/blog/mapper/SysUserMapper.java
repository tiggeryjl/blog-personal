package com.blog.mapper;

import com.blog.pojo.dto.UserPageQueryDTO;
import com.blog.pojo.entity.SysRole;
import com.blog.pojo.entity.SysUser;
import com.blog.pojo.vo.UserPageVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysUserMapper {

    /**
     * 用户注册
     * */
    void register(SysUser user);

    /**
     * 根据用户ID查询角色
     * @param userId
     * @return
     */
    List<SysRole> selectRoleListByUserId(Long userId);

    /**
     * 根据用户名查询用户
     * @param username
     * @return
     */
    @Select("select * from sys_user where username=#{username}")
    SysUser selectByUsername(String username);

    /**
     * 根据手机号查询用户
     * @param phone
     * @return
     */
    @Select("select * from sys_user where phone=#{phone}")
    SysUser selectByPhone(String phone);

    /**
     * 根据邮箱查询用户
     * @param email
     * @return
     */
    @Select("select * from sys_user where email=#{email}")
    SysUser selectByEmail(String email);

    /**
     * 根据id查找用户
     * @param id
     * @return
     */
    @Select("select * from sys_user where id =#{id}")
    SysUser getByUserId(Long id);


    /**
     * 悲观锁查询
     * @param id
     * @return
     */
    @Select("select * from sys_user where id = #{id} and delete_flag = 0 for update")
    SysUser getByIdForUpdate(Long id);

    /**
     * 修改用户
     * @param user
     */
    void update(SysUser user);

    /**
     * 批量查询用户
     * @param ids
     * @return
     */
    List<SysUser> selectByIds(@Param("ids") List<Long> ids);

    /**
     * 批量永久删除
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 批量逻辑删除
     * @param ids
     */
    void deleteBatchLogic(List<Long> ids ,Integer deleteFlag);

    /**
     * 分页查询
     * @param userPageQueryDTO
     * @return
     */
    List<UserPageVo> pageQuery(UserPageQueryDTO userPageQueryDTO);

    /**
     * 分页查询逻辑删除的用户
     * @param userPageQueryDTO
     * @return
     */
    List<UserPageVo> pageQueryLogicDelete(UserPageQueryDTO userPageQueryDTO);
}
