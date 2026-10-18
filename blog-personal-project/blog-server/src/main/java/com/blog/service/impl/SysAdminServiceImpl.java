package com.blog.service.impl;

import com.blog.constant.*;
import com.blog.context.BaseContext;
import com.blog.exception.*;
import com.blog.mapper.SysMenuMapper;
import com.blog.mapper.SysRoleMapper;
import com.blog.mapper.SysUserMapper;
import com.blog.mapper.SysUserRoleMapper;
import com.blog.pojo.dto.*;
import com.blog.pojo.entity.SysMenu;
import com.blog.pojo.entity.SysRole;
import com.blog.pojo.entity.SysUser;
import com.blog.pojo.entity.SysUserRole;
import com.blog.pojo.vo.*;
import com.blog.result.PageResult;
import com.blog.service.SysAdminService;
import com.blog.utils.IpUtil;
import com.blog.utils.MenuTreeUtil;
import com.blog.utils.PasswordGenerateUtil;
import com.blog.utils.PasswordSaltUtil;
import com.blog.utils.UserUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysAdminServiceImpl implements SysAdminService {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired
    private SysMenuMapper menuMapper;

    /**
     * 用户登录
     * @param userLoginDTO
     * @return
     */
    @Override
    public SysUser login(UserLoginDTO userLoginDTO) {
        // 1. 去空格
        String account = userLoginDTO.getLoginName().trim();
        String pwd = userLoginDTO.getPassword().trim();

        // 2. 非空校验
        if (!StringUtils.hasText(account) || !StringUtils.hasText(pwd)) {
            throw new LoginFailedException(MessageConstant.ACCOUNT_PASSWORD_NOT_EMPTY);
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

        List<SysRole> userRoleList = userMapper.selectRoleListByUserId(user.getId());
        user.setRoleList(userRoleList);

        // 5.用户未分配任何角色，禁止登录后台
        if (userRoleList == null || userRoleList.isEmpty()) {
            throw new LoginFailedException(MessageConstant.NOT_ADMIN_ROLE);
        }

        // 6.账号状态校验
        if (MultiStatusConstant.ZERO.equals(user.getStatus())) {
            throw new LoginFailedException(MessageConstant.ACCOUNT_DISABLED);
        } else if (MultiStatusConstant.TWO.equals(user.getStatus())) {
            throw new LoginFailedException(MessageConstant.ACCOUNT_LOGOUT);
        }

        // 7. 密码校验
        String encrypt = PasswordSaltUtil.encryptPwd(pwd, user.getSalt());
        if (!encrypt.equals(user.getPassword())) {
            throw new LoginFailedException(MessageConstant.PASSWORD_ERROR);
        }

        return user;
    }

    /**
     * 获取用户角色权限信息
     * @param username
     * @return
     */
    @Override
    public UserInfoVO getUserInfoByUsername(String username) {
        SysUser user = userMapper.selectByUsername(username);
        // 1.封装用户简单信息
        UserSimpleVO simpleVO = new UserSimpleVO();
        BeanUtils.copyProperties(user, simpleVO);

        // 2.角色key集合
        List<SysRole> roleList = userMapper.selectRoleListByUserId(simpleVO.getId());
        List<String> roles = roleList.stream()
                .map(SysRole::getRoleKey)
                .collect(Collectors.toList());

        // 3.权限标识
        List<String> permissions = menuMapper.selectPermsByUserId(simpleVO.getId());

        // 4.菜单树形
        List<SysMenu> allMenu = menuMapper.selectMenuListByRoleIds(
                roleList.stream().map(SysRole::getId).collect(Collectors.toList())
        );
        List<SysMenu> routerMenu = allMenu.stream()
                .filter(m -> m.getMenuType() != 2)
                .collect(Collectors.toList());
        List<SysMenu> treeMenu = MenuTreeUtil.buildTree(routerMenu);

        UserInfoVO vo = new UserInfoVO();
        vo.setUser(simpleVO);
        vo.setRoles(roles);
        vo.setPermissions(permissions);
        vo.setRouters(treeMenu);
        return vo;
    }

    /**
     * 分页查询
     *
     * @param userPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQurey(UserPageQueryDTO userPageQueryDTO) {
        PageHelper.startPage(userPageQueryDTO.getPage(), userPageQueryDTO.getPageSize());
        List<UserPageVo> userList = userMapper.pageQuery(userPageQueryDTO);
        PageInfo<UserPageVo> page = new PageInfo<>(userList);
        return new PageResult(page.getTotal(), page.getList());
    }

    /**
     * 分页查询逻辑删除的用户
     *
     * @param userPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQueryLogicDelete(UserPageQueryDTO userPageQueryDTO) {
        PageHelper.startPage(userPageQueryDTO.getPage(), userPageQueryDTO.getPageSize());
        List<UserPageVo> userList = userMapper.pageQueryLogicDelete(userPageQueryDTO);
        PageInfo<UserPageVo> page = new PageInfo<>(userList);
        return new PageResult(page.getTotal(), page.getList());
    }

    /**
     * 恢复用户
     *
     * @param id
     */
    @Override
    public void recover(Long id) {
        SysUser user = userMapper.getByUserId(id);
        if (user == null) {
            throw new UserNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        SysUser updateUser = SysUser.builder()
                .id(id)
                .deleteFlag(DelStatusConstant.ENABLE)
                .updateTime(LocalDateTime.now())
                .build();
        userMapper.update(updateUser);
    }

    /**
     * 根据id查询用户
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
     * 修改密码
     * @param passwordEditDTO
     */
    @Override
    public void updatePwd(PasswordEditDTO passwordEditDTO) {
        SysUser user = userMapper.getByUserId(passwordEditDTO.getUserId());
        String oldPassword = passwordEditDTO.getOldPassword().trim();
        String newPassword = passwordEditDTO.getNewPassword().trim();

        String oldEncryptPwd = PasswordSaltUtil.encryptPwd(oldPassword, user.getSalt());

        if (!user.getPassword().equals(oldEncryptPwd)) {
            throw new PasswordEditFailedException(MessageConstant.PASSWORD_ERROR);
        }

        if (oldPassword.equals(newPassword)) {
            throw new PasswordEditFailedException(MessageConstant.PASSWORD_REPEAT);
        }
        if (newPassword.length() < 6 || newPassword.length() > 20) {
            throw new RegisterFailedException(MessageConstant.PASSWORD_LEN_SHORT);
        }

        String newSalt = PasswordSaltUtil.generateSalt();
        String newEncryptPwd = PasswordSaltUtil.encryptPwd(newPassword, newSalt);
        user.setSalt(newSalt);
        user.setPassword(newEncryptPwd);

        userMapper.update(user);
    }

    /**
     * 重置用户密码为随机密码
     * 管理员不需要知道用户的旧密码，重置完把新密码转告用户即可
     */
    @Override
    public String resetPassword(Long userId) {
        if (userId == null) {
            throw new UserNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        // 自己的密码走「修改密码」，那里要验证旧密码，不允许从这个接口绕过
        if (userId.equals(BaseContext.getCurrentId())) {
            throw new OperationNotAllowedException("请使用「修改密码」修改自己的密码");
        }
        // 超管账号保护：避免普通管理员拿到 sys:user:edit 后直接接管超级管理员
        if (userRoleMapper.hasRole(userId, SystemConstant.SUPER_ADMIN_ROLE)) {
            throw new OperationNotAllowedException("超级管理员的密码不可重置");
        }

        SysUser user = userMapper.getByUserId(userId);
        if (user == null || DelStatusConstant.DISABLE.equals(user.getDeleteFlag())) {
            throw new UserNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        String newPassword = PasswordGenerateUtil.generate();
        String newSalt = PasswordSaltUtil.generateSalt();
        user.setSalt(newSalt);
        user.setPassword(PasswordSaltUtil.encryptPwd(newPassword, newSalt));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.update(user);

        return newPassword;
    }

    /**
     * 修改用户信息
     *
     * @param userDTO
     */
    @Override
    public void update(UserDTO userDTO) {
        //校验
        Verification(userDTO.getUsername(), userDTO.getPhone(), userDTO.getEmail(),false);

        SysUser user = new SysUser();
        BeanUtils.copyProperties(userDTO, user);
        user.setUpdateTime(LocalDateTime.now());

        userMapper.update(user);
    }

    /**
     * 修改用户状态
     * @param id
     * @param status
     */
    @Override
    public void updateStatus(Long id, Integer status) {
        if (id == null || (!StatusConstant.ENABLE.equals(status) && !StatusConstant.DISABLE.equals(status))) {
            throw new OperationNotAllowedException("用户状态不合法");
        }
        Long currentId = BaseContext.getCurrentId();
        if (id.equals(currentId)) {
            throw new OperationNotAllowedException("不能启用/禁用自己");
        }

        SysUser target = userMapper.getByUserId(id);
        if (target == null || DelStatusConstant.DISABLE.equals(target.getDeleteFlag())) {
            throw new UserNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 有角色即代表是后台管理员账号；普通博客用户没有角色
        List<SysRoleSimpleVO> targetRoles = userRoleMapper.selectRoleByUserId(id);
        if (targetRoles != null && !targetRoles.isEmpty()) {
            boolean targetIsSuper = targetRoles.stream()
                    .anyMatch(role -> SystemConstant.SUPER_ADMIN_ROLE.equals(role.getRoleKey()));
            if (targetIsSuper) {
                throw new OperationNotAllowedException("超级管理员的账号不可启用/禁用");
            }
            // 普通管理员之间不能相互启用禁用，只有超管能管理普通管理员
            if (!userRoleMapper.hasRole(currentId, SystemConstant.SUPER_ADMIN_ROLE)) {
                throw new OperationNotAllowedException("普通管理员不能启用/禁用其他管理员的账号");
            }
        }

        SysUser user = SysUser.builder().id(id)
                .status(status)
                .updateTime(LocalDateTime.now()).build();
        userMapper.update(user);
    }

    /**
     * 逻辑删除用户
     * @param ids
     */
    @Override
    public void logicDelete(List<Long> ids) {
        SysUser adminUser = userMapper.getByUserId(BaseContext.getCurrentId());

        List<SysUser> targetUsers = userMapper.selectByIds(ids);
        if (targetUsers == null || targetUsers.isEmpty()) {
            throw new UserNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        targetUsers.forEach(targetUser -> {
           if (StatusConstant.ENABLE.equals(targetUser.getStatus())) {
               throw new OperationNotAllowedException("用户启用中，不可删除");
           }
        });

        // 检查是否包含自己
        boolean hasSelf = targetUsers.stream().anyMatch(u -> u.getId().equals(adminUser.getId()));
        if (hasSelf) {
            throw new OperationNotAllowedException(MessageConstant.NOT_DELETE);
        }

        // 批量逻辑删除
        userMapper.deleteBatchLogic(ids, DelStatusConstant.DISABLE);
    }

    /**
     * 彻底删除用户
     * @param ids
     */
    @Override
    @Transactional
    public void delete(List<Long> ids) {
        SysUser adminUser = userMapper.getByUserId(BaseContext.getCurrentId());

        List<SysUser> targetUsers = userMapper.selectByIds(ids);
        if (targetUsers == null || targetUsers.isEmpty()) {
            throw new UserNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 检查是否包含自己
        boolean hasSelf = targetUsers.stream().anyMatch(u -> u.getId().equals(adminUser.getId()));
        if (hasSelf) {
            throw new OperationNotAllowedException(MessageConstant.NOT_DELETE);
        }

        // 清除用户角色关联数据
        userRoleMapper.deleteByUserIds(ids);

        // 批逻彻底删除
        userMapper.deleteBatch(ids);
    }

    /**
     * 获取用户角色集合
     * @return
     */
    @Override
    public List<RoleSelectVO> getRoleList() {
        return roleMapper.getRoleList();
    }

    /**
     * 分配用户角色
     * @param userRoleAssignDTO
     */
    @Transactional
    @Override
    public void updateRole(UserRoleAssignDTO userRoleAssignDTO) {
        List<Long> roleIdList = userRoleAssignDTO.getRoleIdList();

        List<SysRole> allRoles;

        // 查询所有待分配的角色
        if (roleIdList == null || roleIdList.isEmpty()) {
            allRoles = new ArrayList<>();
        } else {
            allRoles = roleMapper.selectRolesByRoleIds(roleIdList);
        }

        boolean hasSuperAdmin = allRoles.stream()
                .anyMatch(r -> "admin".equals(r.getRoleKey()));
        if (hasSuperAdmin) {
            throw new RuntimeException("不允许分配超级管理员角色");
        }

        // 2. 原有超管用户禁止修改角色判断
        List<SysRoleSimpleVO> userRoles = userRoleMapper.selectRoleByUserId(userRoleAssignDTO.getId());
        boolean isSuperUser = userRoles.stream()
                .anyMatch(r -> "admin".equals(r.getRoleKey()));
        if (isSuperUser) {
            throw new RuntimeException("超级管理员不允许修改角色");
        }

        //删除原有数据
        userRoleMapper.deleteByUserIds(List.of(userRoleAssignDTO.getId()));

        //重新批量插入
        if (roleIdList != null && !roleIdList.isEmpty()) {
            List<SysUserRole> batchList = roleIdList.stream()
                    .map(rid -> {
                        SysUserRole ur = new SysUserRole();
                        ur.setUserId(userRoleAssignDTO.getId());
                        ur.setRoleId(rid);
                        ur.setCreateTime(LocalDateTime.now());
                        return ur;
                    }).collect(Collectors.toList());
            userRoleMapper.batchInsert(batchList);
        }
    }

    /**
     * 新增用户
     * @param userDTO
     */
    @Override
    public void add(UserDTO userDTO) {
        //校验
        Verification(userDTO.getUsername(), userDTO.getPhone(), userDTO.getEmail(),true);

        SysUser user = new SysUser();
        BeanUtils.copyProperties(userDTO, user);
        if (!StringUtils.hasText(user.getNickname())) {
            user.setNickname(MessageConstant.NICKNAME + UserUtil.generateRandomNum());
        }
        String salt = PasswordSaltUtil.generateSalt();
        String pwd = PasswordSaltUtil.encryptPwd(PasswordConstant.DEFAULT_PASSWORD, salt);
        user.setSalt(salt);
        user.setPassword(pwd);
        if (StringUtils.hasText(user.getAvatar())){
            user.setAvatar(user.getAvatar());
        }else {
            user.setAvatar(UserUtil.getQQAvatarUrl(user.getEmail()));
        }
        user.setStatus(MultiStatusConstant.ONE);
        user.setDeleteFlag(DelStatusConstant.ENABLE);
        user.setLockFlag(DelStatusConstant.ENABLE);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        userMapper.register(user);
    }

    /**
     * 统一校验：注册 + 修改个人信息
     *
     * @param isRegister true=注册（所有字段必填） false=修改（字段可为空，有值才校验）
     */
    public void Verification(String username, String phone, String email, boolean isRegister) {
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
