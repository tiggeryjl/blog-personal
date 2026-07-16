package com.blog.service.impl;

import com.blog.constant.DelStatusConstant;
import com.blog.constant.MessageConstant;
import com.blog.constant.MultiStatusConstant;
import com.blog.constant.StatusConstant;
import com.blog.exception.LoginFailedException;
import com.blog.exception.PasswordEditFailedException;
import com.blog.exception.RegisterFailedException;
import com.blog.mapper.SysUserMapper;
import com.blog.pojo.dto.PasswordEditDTO;
import com.blog.pojo.dto.UserDTO;
import com.blog.pojo.dto.UserLoginDTO;
import com.blog.pojo.dto.UserRegisterDTO;
import com.blog.pojo.entity.SysUser;
import com.blog.pojo.vo.UserSimpleVO;
import com.blog.service.UserService;
import com.blog.utils.PasswordSaltUtil;
import com.blog.utils.UserUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private SysUserMapper userMapper;

    /**
     * 用户注册
     *
     * @param userRegisterDTO
     */
    @Override
    public void register(UserRegisterDTO userRegisterDTO) {
        String username = userRegisterDTO.getUsername().trim();
        String phone = userRegisterDTO.getPhone().trim();
        String email = userRegisterDTO.getEmail().trim();
        String pwd = userRegisterDTO.getPassword().trim();
        String confirmPwd = userRegisterDTO.getConfirmPwd().trim();

        //校验
        Verification(username, phone, email,true);

        // ===================== 4. 单独校验密码 =====================
        if (pwd.length() < 6 || pwd.length() > 20) {
            throw new RegisterFailedException(MessageConstant.PASSWORD_LEN_SHORT);
        }
        if (!pwd.equals(confirmPwd)) {
            throw new RegisterFailedException(MessageConstant.PASSWORD_TWO_NOT_EQUAL);
        }


        // ===================== 5. 全部校验通过,插入数据 =====================
        String salt = PasswordSaltUtil.generateSalt();
        String encryptPwd = PasswordSaltUtil.encryptPwd(pwd, salt);
        userRegisterDTO.setPassword(encryptPwd);

        SysUser user = new SysUser();
        BeanUtils.copyProperties(userRegisterDTO, user);

        // 为空：null / 空串 / 全空格
        if (!StringUtils.hasText(user.getNickname())) {
            user.setNickname(MessageConstant.NICKNAME + UserUtil.generateRandomNum());
        }
        user.setAvatar(UserUtil.getQQAvatarUrl(email));
        user.setSex(MultiStatusConstant.TWO);
        user.setSalt(salt);
        user.setStatus(MultiStatusConstant.ONE);
        user.setDeleteFlag(DelStatusConstant.ENABLE);
        user.setLockFlag(DelStatusConstant.ENABLE);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        userMapper.register(user);
    }

    /**
     * 用户登录
     *
     * @param userLoginDTO
     * @return
     */
    @Override
    public SysUser login(UserLoginDTO userLoginDTO) {
        // 1. 去空格
        String account = userLoginDTO.getLoginName().trim();
        String pwd = userLoginDTO.getPassword().trim();

        // 2. 非空校验
        if (!StringUtils.hasText(account)) {
            throw new LoginFailedException(MessageConstant.ACCOUNT_NOT_EMPTY);
        }
        if (!StringUtils.hasText(pwd)) {
            throw new LoginFailedException(MessageConstant.PASSWORD_NOT_EMPTY);
        }

        // 3. 自动识别类型
        SysUser user = null;
        if (account.matches(UserUtil.PHONE_REGEX)) {
            // 手机号登录
            user = userMapper.selectByPhone(account);
        } else if (account.contains("@")) {
            // 邮箱登录
            user = userMapper.selectByEmail(account);
        } else {
            // 用户名登录
            user = userMapper.selectByUsername(account);
        }

        // 4. 账号是否存在
        if (user == null) {
            throw new LoginFailedException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 5. 账号状态校验
        if (MultiStatusConstant.ZERO.equals(user.getStatus())) {
            throw new LoginFailedException(MessageConstant.ACCOUNT_DISABLED);
        } else if (MultiStatusConstant.TWO.equals(user.getStatus())) {
            throw new LoginFailedException(MessageConstant.ACCOUNT_LOGOUT);
        }

        // 6. 密码校验
        String encrypt = PasswordSaltUtil.encryptPwd(pwd, user.getSalt());
        if (!encrypt.equals(user.getPassword())) {
            throw new LoginFailedException(MessageConstant.PASSWORD_ERROR);
        }

        // 7. 登录成功，返回对象并生成token
        return user;
    }

    /**
     * 修改密码
     *
     * @param passwordEditDTO
     */
    @Override
    public void updatePwd(PasswordEditDTO passwordEditDTO) {
        SysUser user = userMapper.getByUserId(passwordEditDTO.getUserId());
        String oldPassword = passwordEditDTO.getOldPassword().trim();
        String newPassword = passwordEditDTO.getNewPassword().trim();
        String confirmPwd = passwordEditDTO.getConfirmPwd().trim();

        String oldEncryptPwd = PasswordSaltUtil.encryptPwd(passwordEditDTO.getOldPassword(), user.getSalt());

        if (!user.getPassword().equals(oldEncryptPwd)) {
            throw new PasswordEditFailedException(MessageConstant.PASSWORD_ERROR);
        }

        if (oldPassword.equals(newPassword)) {
            throw new PasswordEditFailedException(MessageConstant.PASSWORD_REPEAT);
        }

        if (newPassword.length() < 6 || newPassword.length() > 20) {
            throw new RegisterFailedException(MessageConstant.PASSWORD_LEN_SHORT);
        }
        if (!newPassword.equals(confirmPwd)) {
            throw new RegisterFailedException(MessageConstant.PASSWORD_TWO_NOT_EQUAL);
        }

        String newSalt = PasswordSaltUtil.generateSalt();
        String newEncryptPwd = PasswordSaltUtil.encryptPwd(newPassword, newSalt);
        user.setSalt(newSalt);
        user.setPassword(newEncryptPwd);

        userMapper.update(user);
    }

    /**
     * 根据id查询用户
     *
     * @param id
     * @return
     */
    @Override
    public UserSimpleVO getUserInfo(Long id) {
        SysUser user = userMapper.getByUserId(id);
        UserSimpleVO userVo = new UserSimpleVO();
        BeanUtils.copyProperties(user, userVo);
        return userVo;
    }

    /**
     * 修改用户信息
     *
     * @param userDTO
     */
    @Override
    public void update(UserDTO userDTO) {
        String username = userDTO.getUsername();
        String phone = userDTO.getPhone();
        String email = userDTO.getEmail();

        //校验
        Verification(username, phone, email,false);

        SysUser user = new SysUser();
        BeanUtils.copyProperties(userDTO, user);
        userMapper.update(user);
    }

    /**
     * 统一校验：注册 + 修改个人信息
     * @param isRegister true=注册（所有字段必填） false=修改（字段可为空，有值才校验）
     */
    public void Verification(String username, String phone, String email,boolean  isRegister) {
        // ===================== 注册：字段不能为空 =====================
        if (isRegister) {
            if (!StringUtils.hasText(username)) {
                throw new RegisterFailedException("用户名不能为空");
            }
            if (!StringUtils.hasText(phone)) {
                throw new RegisterFailedException("手机号不能为空");
            }
            if (!StringUtils.hasText(email)) {
                throw new RegisterFailedException("邮箱不能为空");
            }
        }

        // ===================== 1. 单独校验用户名（有值才校） =====================
        if (StringUtils.hasText(username)) {
            if (!username.matches(UserUtil.USERNAME_REG)) {
                throw new RegisterFailedException(MessageConstant.USERNAME_RESTRICTION);
            }
            // 用户名不能是手机号格式
            if (username.matches(UserUtil.PHONE_REGEX)) {
                throw new RegisterFailedException(MessageConstant.USERNAME_NOT_PHONE);
            }
            // 用户名不能是邮箱格式
            if (username.matches(UserUtil.EMAIL_REGEX)) {
                throw new RegisterFailedException(MessageConstant.USERNAME_NOT_EMAIL);
            }
            // 用户名是否已注册
            if (userMapper.selectByUsername(username) != null) {
                throw new RegisterFailedException(MessageConstant.USERNAME_EXISTS);
            }
        }

        // ===================== 2. 单独校验手机号（有值才校） =====================
        if (StringUtils.hasText(phone)) {
            if (!phone.matches(UserUtil.PHONE_REGEX)) {
                throw new RegisterFailedException(MessageConstant.PHONE_FORMAT_ERROR);
            }
            if (userMapper.selectByPhone(phone) != null) {
                throw new RegisterFailedException(MessageConstant.PHONE_EXISTS);
            }
        }

        // ===================== 3. 单独校验邮箱（有值才校） =====================
        if (StringUtils.hasText(email)) {
            if (!email.matches(UserUtil.EMAIL_REGEX)) {
                throw new RegisterFailedException(MessageConstant.EMAIL_FORMAT_ERROR);
            }
            if (userMapper.selectByEmail(email) != null) {
                throw new RegisterFailedException(MessageConstant.EMAIL_EXISTS);
            }
        }
    }
}
