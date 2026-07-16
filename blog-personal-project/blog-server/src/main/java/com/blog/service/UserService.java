package com.blog.service;

import com.blog.pojo.dto.PasswordEditDTO;
import com.blog.pojo.dto.UserDTO;
import com.blog.pojo.dto.UserLoginDTO;
import com.blog.pojo.dto.UserRegisterDTO;
import com.blog.pojo.entity.SysUser;
import com.blog.pojo.vo.UserInfoVO;
import com.blog.pojo.vo.UserSimpleVO;

public interface UserService {

    /**
     * 用户注册
     * @param userRegisterDTO
     */
    void register(UserRegisterDTO userRegisterDTO);

    /**
     * 用户登录
     * @param userLoginDTO
     * @return
     */
    SysUser login(UserLoginDTO userLoginDTO);

    /**
     * 修改密码
     * @param passwordEditDTO
     */
    void updatePwd(PasswordEditDTO passwordEditDTO);

    /**
     * 根据id查询用户
     * @param id
     * @return
     */
    UserSimpleVO getUserInfo(Long id);

    /**
     * 修改用户信息
     * @param userDTO
     */
    void update(UserDTO userDTO);
}
