package com.blog.service.impl;

import com.blog.constant.*;
import com.blog.exception.LoginFailedException;
import com.blog.exception.PasswordEditFailedException;
import com.blog.exception.RegisterFailedException;
import com.blog.mapper.SysUserMapper;
import com.blog.pojo.dto.EmailRegisterDTO;
import com.blog.pojo.dto.PasswordEditDTO;
import com.blog.pojo.dto.ResetPasswordDTO;
import com.blog.pojo.dto.ResetPasswordVerifyDTO;
import com.blog.pojo.dto.UserDTO;
import com.blog.pojo.dto.UserLoginDTO;
import com.blog.pojo.dto.UserRegisterDTO;
import com.blog.pojo.entity.SysUser;
import com.blog.pojo.vo.UserPersonalInfoVO;
import com.blog.pojo.vo.UserSimpleVO;
import com.blog.service.MailService;
import com.blog.service.LoginVerificationService;
import com.blog.service.RedisService;
import com.blog.service.UserService;
import com.blog.utils.PasswordSaltUtil;
import com.blog.utils.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private MailService mailService;

    @Autowired
    private LoginVerificationService loginVerificationService;

    /**
     * 用户注册
     *
     * @param userRegisterDTO
     */
    @Override
    public void register(UserRegisterDTO userRegisterDTO) {
        String username = normalizeOptional(userRegisterDTO.getUsername());
        String phone = normalizeOptional(userRegisterDTO.getPhone());
        String email = normalizeEmail(userRegisterDTO.getEmail());
        String pwd = normalizeOptional(userRegisterDTO.getPassword());
        String confirmPwd = normalizeOptional(userRegisterDTO.getConfirmPwd());

        //校验
        Verification(username, phone, email,true);

        // ===================== 单独校验密码 =====================
        if (pwd.length() < 6 || pwd.length() > 20) {
            throw new RegisterFailedException(MessageConstant.PASSWORD_LEN_SHORT);
        }
        if (!pwd.equals(confirmPwd)) {
            throw new RegisterFailedException(MessageConstant.PASSWORD_TWO_NOT_EQUAL);
        }

        // 校验注册邮箱验证码，成功后立即失效
        verifyRegisterCode(email, userRegisterDTO.getEmailCode());

        // ===================== 5. 全部校验通过,插入数据 =====================
        String salt = PasswordSaltUtil.generateSalt();
        String encryptPwd = PasswordSaltUtil.encryptPwd(pwd, salt);
        userRegisterDTO.setPassword(encryptPwd);

        SysUser user = new SysUser();
        BeanUtils.copyProperties(userRegisterDTO, user);
        user.setUsername(StringUtils.hasText(username) ? username : buildUniqueUsername(email));
        user.setPhone(StringUtils.hasText(phone) ? phone : null);
        user.setEmail(email);

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
     * 发送注册邮箱验证码
     *
     * @param email 注册邮箱
     * @param ip 请求IP
     */
    @Override
    public void sendRegisterCode(String email, String ip) {
        String mail = normalizeEmail(email);
        if (!StringUtils.hasText(mail)) {
            throw new RegisterFailedException("邮箱不能为空");
        }
        if (!mail.matches(UserUtil.EMAIL_REGEX)) {
            throw new RegisterFailedException(MessageConstant.EMAIL_FORMAT_ERROR);
        }
        if (userMapper.selectByEmail(mail) != null) {
            throw new RegisterFailedException(MessageConstant.EMAIL_EXISTS);
        }
        if (!mailService.isAvailable()) {
            throw new RegisterFailedException("站点还没有配置发件邮箱，暂时无法发送验证码");
        }

        String intervalKey = RegisterVerificationConstant.LIMIT_KEY_PREFIX + mail;
        if (!redisService.setIfAbsent(intervalKey,"1",
                RegisterVerificationConstant.SEND_INTERVAL_SECONDS)) {
            throw new RegisterFailedException("验证码发送太频繁了，请稍后再试");
        }

        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String emailDailyKey = RegisterVerificationConstant.EMAIL_DAILY_KEY_PREFIX
                + date + ":" + mail;
        long emailCount = redisService.incrementAndExpire(
                emailDailyKey,
                1,
                RegisterVerificationConstant.DAILY_COUNT_TTL_SECONDS
        );
        if (emailCount > RegisterVerificationConstant.EMAIL_DAILY_LIMIT) {
            throw new RegisterFailedException("该邮箱今日验证码发送次数已达上限");
        }

        String safeIp = StringUtils.hasText(ip) ? ip.trim() : "unknown";
        if (safeIp.length() > 100) {
            safeIp = safeIp.substring(0, 100);
        }
        String ipDailyKey = RegisterVerificationConstant.IP_DAILY_KEY_PREFIX
                + date + ":" + safeIp;
        long ipCount = redisService.incrementAndExpire(
                ipDailyKey,
                1,
                RegisterVerificationConstant.DAILY_COUNT_TTL_SECONDS
        );
        if (ipCount > RegisterVerificationConstant.IP_DAILY_LIMIT) {
            throw new RegisterFailedException("当前网络今日验证码发送次数已达上限");
        }

        String code = String.format("%06d", RANDOM.nextInt(1000000));
        String codeKey = RegisterVerificationConstant.CODE_KEY_PREFIX + mail;
        String attemptKey = RegisterVerificationConstant.ATTEMPT_KEY_PREFIX + mail;
        redisService.set(codeKey, code, RegisterVerificationConstant.CODE_TTL_SECONDS);
        redisService.delete(attemptKey);

        try {
            mailService.sendHtml(mail,"【小叶同学的博客】注册验证码",buildRegisterCodeHtml(code));
        } catch (Exception e) {
            redisService.delete(codeKey);
            redisService.delete(attemptKey);
            redisService.delete(intervalKey);
            log.error("注册验证码发送失败，邮箱：{}", mail, e);
            throw new RegisterFailedException("验证码发送失败，请稍后重试");
        }
    }

    /**
     * 判断邮箱是否已注册
     *
     * @param email 邮箱
     * @return true=已注册
     */
    @Override
    public boolean existsByEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }
        return userMapper.selectByEmail(email.trim()) != null;
    }

    /**
     * 邮箱快捷注册（留言板简约注册，仅需邮箱与密码）
     *
     * @param emailRegisterDTO 邮箱、密码与确认密码
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUser registerByEmail(EmailRegisterDTO emailRegisterDTO) {
        String email = normalizeEmail(emailRegisterDTO.getEmail());
        String nickname = emailRegisterDTO.getNickname() == null ? "" : emailRegisterDTO.getNickname().trim();
        String pwd = emailRegisterDTO.getPassword() == null ? "" : emailRegisterDTO.getPassword().trim();
        String confirmPwd = emailRegisterDTO.getConfirmPwd() == null
                ? "" : emailRegisterDTO.getConfirmPwd().trim();

        if (!StringUtils.hasText(email)) {
            throw new RegisterFailedException("邮箱不能为空");
        }
        if (!email.matches(UserUtil.EMAIL_REGEX)) {
            throw new RegisterFailedException(MessageConstant.EMAIL_FORMAT_ERROR);
        }
        if (userMapper.selectByEmail(email) != null) {
            throw new RegisterFailedException(MessageConstant.EMAIL_EXISTS);
        }
        if (pwd.length() < 6 || pwd.length() > 20) {
            throw new RegisterFailedException(MessageConstant.PASSWORD_LEN_SHORT);
        }
        if (!pwd.equals(confirmPwd)) {
            throw new RegisterFailedException(MessageConstant.PASSWORD_TWO_NOT_EQUAL);
        }

        // 快捷注册与普通注册共用同一套邮箱验证码
        verifyRegisterCode(email, emailRegisterDTO.getEmailCode());

        String salt = PasswordSaltUtil.generateSalt();
        String nickPrefix = email.substring(0, email.indexOf('@'));
        String finalNickname = StringUtils.hasText(nickname) ? nickname : nickPrefix;
        if (finalNickname.length() > 50) {
            finalNickname = finalNickname.substring(0, 50);
        }

        SysUser user = new SysUser();
        user.setUsername(buildUniqueUsername(email));
        user.setNickname(StringUtils.hasText(finalNickname)
                ? finalNickname : MessageConstant.NICKNAME + UserUtil.generateRandomNum());
        user.setEmail(email);
        user.setPhone(null);
        user.setPassword(PasswordSaltUtil.encryptPwd(pwd, salt));
        user.setSalt(salt);
        user.setAvatar(UserUtil.getQQAvatarUrl(email));
        user.setSex(MultiStatusConstant.TWO);
        user.setStatus(MultiStatusConstant.ONE);
        user.setDeleteFlag(DelStatusConstant.ENABLE);
        user.setLockFlag(DelStatusConstant.ENABLE);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.register(user);
        return userMapper.selectByEmail(email);
    }

    /**
     * 由邮箱前缀生成唯一用户名（3-16位字母/数字/下划线）
     *
     * @param email 邮箱
     * @return 唯一用户名
     */
    private String buildUniqueUsername(String email) {
        String prefix = email.substring(0, email.indexOf('@')).replaceAll("[^a-zA-Z0-9_]", "_");
        if (prefix.length() < 3) {
            prefix = prefix + "_user";
        }
        if (prefix.length() > 12) {
            prefix = prefix.substring(0, 12);
        }

        String candidate = prefix;
        int suffix = 1;
        while (userMapper.selectByUsername(candidate) != null) {
            String suffixText = String.valueOf(suffix++);
            int baseLength = Math.max(3, 16 - suffixText.length());
            String base = prefix.length() > baseLength ? prefix.substring(0, baseLength) : prefix;
            candidate = base + suffixText;
        }
        return candidate;
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
            loginVerificationService.recordPasswordFailure(user.getId());
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
     * 忘记密码：发送邮箱验证码
     * 同一个邮箱 60 秒内只能发一次，验证码 5 分钟内有效
     */
    @Override
    public void sendResetPwdCode(String email) {
        String mail = normalizeEmail(email);
        if (!StringUtils.hasText(mail)) {
            throw new RegisterFailedException("邮箱不能为空");
        }
        if (userMapper.selectByEmail(mail) == null) {
            throw new RegisterFailedException("该邮箱还没有注册");
        }
        // sendHtml 在未配置发件邮箱时会静默跳过，这里提前拦掉，避免用户一直等一封不会来的邮件
        if (!mailService.isAvailable()) {
            throw new RegisterFailedException("站点还没有配置发件邮箱，暂时无法发送验证码");
        }

        String limitKey = ResetPasswordConstant.LIMIT_KEY_PREFIX + mail;
        if (Boolean.TRUE.equals(redisService.hasKey(limitKey))) {
            throw new RegisterFailedException("验证码发送太频繁了，请稍后再试");
        }

        String code = String.format("%06d", RANDOM.nextInt(1000000));
        String codeKey = ResetPasswordConstant.CODE_KEY_PREFIX + mail;
        redisService.set(codeKey, code, ResetPasswordConstant.CODE_TTL_SECONDS);
        redisService.set(limitKey, "1", ResetPasswordConstant.SEND_INTERVAL_SECONDS);

        try {
            mailService.sendHtml(mail, "【小叶同学的博客】重置密码验证码", buildResetCodeHtml(code));
        } catch (Exception e) {
            // 发信失败要把验证码和限流一起清掉
            redisService.delete(codeKey);
            redisService.delete(limitKey);
            log.error("重置密码验证码发送失败，邮箱：{}", mail, e);
            throw new RegisterFailedException("验证码发送失败，请稍后重试");
        }
    }

    /**
     * 忘记密码：校验邮箱验证码，通过后发放一次性凭证
     */
    @Override
    public String verifyResetPwdCode(ResetPasswordVerifyDTO resetPasswordVerifyDTO) {
        String mail = normalizeEmail(resetPasswordVerifyDTO.getEmail());
        String emailCode = trimToEmpty(resetPasswordVerifyDTO.getEmailCode());

        if (!StringUtils.hasText(mail)) {
            throw new RegisterFailedException("邮箱不能为空");
        }
        if (userMapper.selectByEmail(mail) == null) {
            throw new RegisterFailedException("该邮箱还没有注册");
        }

        String cachedCode = redisService.get(ResetPasswordConstant.CODE_KEY_PREFIX + mail, String.class);
        if (!StringUtils.hasText(cachedCode)) {
            throw new RegisterFailedException("验证码已过期，请重新获取");
        }
        if (!cachedCode.equals(emailCode)) {
            throw new RegisterFailedException("验证码不正确");
        }

        String ticket = UUID.randomUUID().toString().replace("-", "");
        redisService.set(ResetPasswordConstant.TICKET_KEY_PREFIX + ticket, mail,
                ResetPasswordConstant.TICKET_TTL_SECONDS);
        return ticket;
    }

    /**
     * 忘记密码：凭一次性凭证重置密码
     * 凭证与验证码都在改密成功后才作废，中途失败可以重试
     */
    @Override
    public void resetPwdByTicket(ResetPasswordDTO resetPasswordDTO) {
        String ticket = trimToEmpty(resetPasswordDTO.getTicket());
        String newPassword = trimToEmpty(resetPasswordDTO.getNewPassword());
        String confirmPwd = trimToEmpty(resetPasswordDTO.getConfirmPwd());

        String ticketKey = ResetPasswordConstant.TICKET_KEY_PREFIX + ticket;
        String mail = StringUtils.hasText(ticket) ? redisService.get(ticketKey, String.class) : null;
        if (!StringUtils.hasText(mail)) {
            throw new RegisterFailedException("验证已过期，请重新验证邮箱");
        }

        SysUser user = userMapper.selectByEmail(mail);
        if (user == null) {
            throw new RegisterFailedException("该邮箱还没有注册");
        }
        if (newPassword.length() < 6 || newPassword.length() > 20) {
            throw new RegisterFailedException(MessageConstant.PASSWORD_LEN_SHORT);
        }
        if (!newPassword.equals(confirmPwd)) {
            throw new RegisterFailedException(MessageConstant.PASSWORD_TWO_NOT_EQUAL);
        }

        String encryptedNewPwd = PasswordSaltUtil.encryptPwd(newPassword, user.getSalt());
        if (encryptedNewPwd.equals(user.getPassword())) {
            throw new RegisterFailedException(MessageConstant.PASSWORD_REPEAT);
        }

        String newSalt = PasswordSaltUtil.generateSalt();
        user.setSalt(newSalt);
        user.setPassword(PasswordSaltUtil.encryptPwd(newPassword, newSalt));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.update(user);

        // 改密成功，凭证和验证码一起作废
        redisService.delete(ticketKey);
        redisService.delete(ResetPasswordConstant.CODE_KEY_PREFIX + mail);
    }

    /**
     * 邮箱统一去空格转小写
     */
    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    /**
     * 可选字符串去空格，null 转成空字符串
     *
     * @param value 原始值
     * @return 标准化后的值
     */
    private String normalizeOptional(String value) {
        return value == null ? "" : value.trim();
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * 重置密码验证码的邮件正文
     */
    private String buildResetCodeHtml(String code) {
        return "<div style=\"font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#333;line-height:1.9\">"
                + "<p>您好，</p>"
                + "<p>您正在重置「小叶同学的博客」的登录密码，本次验证码为：</p>"
                + "<p style=\"font-size:26px;font-weight:bold;letter-spacing:6px;color:#4f46e5\">" + code + "</p>"
                + "<p>验证码 " + (ResetPasswordConstant.CODE_TTL_SECONDS / 60) + " 分钟内有效，请勿转发给他人。</p>"
                + "<p>如果这不是您本人的操作，忽略这封邮件即可，您的密码不会被修改。</p>"
                + "</div>";
    }

    /**
     * 校验注册邮箱验证码
     *
     * @param email 注册邮箱
     * @param emailCode 用户输入的验证码
     */
    private void verifyRegisterCode(String email, String emailCode) {
        String codeKey = RegisterVerificationConstant.CODE_KEY_PREFIX + email;
        String attemptKey = RegisterVerificationConstant.ATTEMPT_KEY_PREFIX + email;
        String cachedCode = redisService.get(codeKey, String.class);
        if (!StringUtils.hasText(cachedCode)) {
            throw new RegisterFailedException("验证码已过期，请重新获取");
        }

        String inputCode = trimToEmpty(emailCode);
        if (!cachedCode.equals(inputCode)) {
            long attempts = redisService.incrementAndExpire(
                    attemptKey,
                    1,
                    RegisterVerificationConstant.CODE_TTL_SECONDS
            );
            if (attempts >= RegisterVerificationConstant.MAX_ATTEMPTS) {
                redisService.delete(codeKey);
                redisService.delete(attemptKey);
                throw new RegisterFailedException("验证码错误次数过多，请重新获取");
            }
            throw new RegisterFailedException("验证码不正确");
        }

        redisService.delete(codeKey);
        redisService.delete(attemptKey);
    }

    /**
     * 注册验证码邮件正文
     *
     * @param code 验证码
     * @return HTML正文
     */
    private String buildRegisterCodeHtml(String code) {
        return "<div style=\"font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#333;line-height:1.9\">"
                + "<p>您好，</p>"
                + "<p>您正在注册「小叶同学的博客」账号，本次验证码为：</p>"
                + "<p style=\"font-size:26px;font-weight:bold;letter-spacing:6px;color:#4f46e5\">" + code + "</p>"
                + "<p>验证码 5 分钟内有效，请勿转发给他人。</p>"
                + "<p>如果这不是您本人的操作，请忽略这封邮件。</p>"
                + "</div>";
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
     * 获取个人信息
     * @return
     */
    @Override
    public UserPersonalInfoVO getPersonalInfo() {
        SysUser admin = userMapper.getByUserId(LayoutConstant.ADMIN_ID);
        UserPersonalInfoVO userPersonalInfoVO = new UserPersonalInfoVO();
        BeanUtils.copyProperties(admin, userPersonalInfoVO);
        return userPersonalInfoVO;
    }

    /**
     * 统一校验：注册 + 修改个人信息
     * @param isRegister true=注册（所有字段必填） false=修改（字段可为空，有值才校验）
     */
    public void Verification(String username, String phone, String email,boolean  isRegister) {
        // ===================== 注册：仅邮箱必填，用户名和手机号可选 =====================
        if (isRegister) {
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
