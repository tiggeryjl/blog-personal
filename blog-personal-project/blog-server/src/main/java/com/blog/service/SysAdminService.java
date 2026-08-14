package com.blog.service;

import com.blog.pojo.dto.*;
import com.blog.pojo.entity.SysUser;
import com.blog.pojo.vo.RoleSelectVO;
import com.blog.pojo.vo.UserInfoVO;
import com.blog.pojo.vo.UserSimpleVO;
import com.blog.result.PageResult;

import java.util.List;

public interface SysAdminService {

    /**
     * 用户登录
     * @param userLoginDTO
     * @return
     */
    SysUser login(UserLoginDTO userLoginDTO);

    /**
     * 获取用户角色权限信息
     * @param username
     * @return
     */
    UserInfoVO getUserInfoByUsername(String username);

    /**
     * 分页查询
     * @param userPageQueryDTO
     * @return
     */
    PageResult pageQurey(UserPageQueryDTO userPageQueryDTO);

    /**
     * 分页查询逻辑删除的用户
     * @param userPageQueryDTO
     * @return
     */
    PageResult pageQueryLogicDelete(UserPageQueryDTO userPageQueryDTO);

    /**
     * 恢复用户
     * @param id
     */
    void recover(Long id);

    /**
     * 根据id查询用户
     * @param id
     * @return
     */
    UserSimpleVO getUserInfo(Long id);

    /**
     * 修改密码
     * @param passwordEditDTO
     */
    void updatePwd(PasswordEditDTO passwordEditDTO);

    /**
     * 修改用户信息
     * @param userDTO
     */
    void update(UserDTO userDTO);

    /**
     * 修改用户状态
     * @param id
     * @param status
     */
    void updateStatus(Long id, Integer status);

    /**
     * 逻辑删除用户
     * @param ids
     */
    void logicDelete(List<Long> ids);

    /**
     * 彻底删除用户
     * @param ids
     */
    void delete(List<Long> ids);

    /**
     * 获取用户角色集合
     * @return
     */
    List<RoleSelectVO> getRoleList();

    /**
     * 分配用户角色
     * @param userRoleAssignDTO
     */
    void updateRole(UserRoleAssignDTO userRoleAssignDTO);

    /**
     * 新增用户
     * @param userDTO
     */
    void add(UserDTO userDTO);
}
