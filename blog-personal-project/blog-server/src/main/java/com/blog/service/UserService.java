package com.blog.service;

import com.blog.pojo.dto.EmailRegisterDTO;
import com.blog.pojo.dto.PasswordEditDTO;
import com.blog.pojo.dto.ResetPasswordDTO;
import com.blog.pojo.dto.ResetPasswordVerifyDTO;
import com.blog.pojo.dto.UserDTO;
import com.blog.pojo.dto.UserLoginDTO;
import com.blog.pojo.dto.UserRegisterDTO;
import com.blog.pojo.entity.SysUser;
import com.blog.pojo.vo.UserInfoVO;
import com.blog.pojo.vo.UserPersonalInfoVO;
import com.blog.pojo.vo.UserSimpleVO;

public interface UserService {

    /**
     * 用户注册
     * @param userRegisterDTO
     */
    void register(UserRegisterDTO userRegisterDTO);

    /**
     * 判断邮箱是否已注册
     *
     * @param email 邮箱
     * @return true=已注册
     */
    boolean existsByEmail(String email);

    /**
     * 邮箱快捷注册（留言板简约注册，仅需邮箱与密码）
     *
     * @param emailRegisterDTO 邮箱、密码与确认密码
     */
    void registerByEmail(EmailRegisterDTO emailRegisterDTO);

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
     * 忘记密码：发送邮箱验证码
     *
     * @param email 注册邮箱
     */
    void sendResetPwdCode(String email);

    /**
     * 忘记密码：校验邮箱验证码，通过后返回一次性凭证
     *
     * @param resetPasswordVerifyDTO 邮箱与验证码
     * @return 重置密码用的一次性凭证
     */
    String verifyResetPwdCode(ResetPasswordVerifyDTO resetPasswordVerifyDTO);

    /**
     * 忘记密码：凭一次性凭证重置密码
     *
     * @param resetPasswordDTO 凭证与新密码
     */
    void resetPwdByTicket(ResetPasswordDTO resetPasswordDTO);

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

    /**
     * 获取个人信息
     * @return
     */
    UserPersonalInfoVO getPersonalInfo();
}
