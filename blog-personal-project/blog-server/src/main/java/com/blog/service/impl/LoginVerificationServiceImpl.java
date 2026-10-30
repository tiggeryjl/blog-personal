package com.blog.service.impl;

import com.blog.constant.DelStatusConstant;
import com.blog.constant.LoginVerificationConstant;
import com.blog.constant.MultiStatusConstant;
import com.blog.exception.LoginFailedException;
import com.blog.mapper.SysUserMapper;
import com.blog.mapper.UserDeviceMapper;
import com.blog.pojo.dto.LoginRiskTicketDTO;
import com.blog.pojo.dto.LoginVerifyDTO;
import com.blog.pojo.entity.SysUser;
import com.blog.pojo.entity.UserDevice;
import com.blog.pojo.vo.DeviceInfoVO;
import com.blog.pojo.vo.LoginRiskInfoVO;
import com.blog.service.LoginVerificationService;
import com.blog.service.MailService;
import com.blog.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 登录风险评估与邮箱验证码实现
 */
@Slf4j
@Service
public class LoginVerificationServiceImpl implements LoginVerificationService {

    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private UserDeviceMapper userDeviceMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private MailService mailService;

    @Override
    public String getRiskReason(Long userId, String email, String deviceId) {
        long failures = redisService.getCounter(
                LoginVerificationConstant.FAILURE_KEY_PREFIX + userId
        );
        if (failures >= LoginVerificationConstant.FAILURE_THRESHOLD) {
            return "密码错误次数过多";
        }

        if (StringUtils.hasText(email)
                && StringUtils.hasText(deviceId)
                && userDeviceMapper.countTrustedDevice(userId, deviceId.trim()) == 0) {
            return "新设备登录";
        }
        return null;
    }

    @Override
    public LoginRiskInfoVO createRiskTicket(
            SysUser user, String deviceId, String ip, String riskReason) {
        String email = normalizeEmail(user.getEmail());
        if (!StringUtils.hasText(email)) {
            throw new LoginFailedException("账号未绑定邮箱，无法完成登录安全验证");
        }

        String ticket = UUID.randomUUID().toString().replace("-", "");
        LoginRiskTicketDTO ticketData = LoginRiskTicketDTO.builder()
                .userId(user.getId())
                .email(email)
                .deviceId(trimToEmpty(deviceId))
                .ip(trimToEmpty(ip))
                .riskReason(riskReason)
                .build();
        redisService.set(
                LoginVerificationConstant.RISK_KEY_PREFIX + ticket,
                ticketData,
                LoginVerificationConstant.RISK_TTL_SECONDS
        );

        return LoginRiskInfoVO.builder()
                .riskTicket(ticket)
                .maskedEmail(maskEmail(email))
                .expiresIn((int) LoginVerificationConstant.RISK_TTL_SECONDS)
                .riskReason(riskReason)
                .build();
    }

    @Override
    public void sendLoginCode(String riskTicket, String ip) {
        LoginRiskTicketDTO ticketData = getRiskTicket(riskTicket);
        SysUser user = userMapper.getByUserId(ticketData.getUserId());
        validateUser(user);

        String email = normalizeEmail(ticketData.getEmail());
        if (!mailService.isAvailable()) {
            throw new LoginFailedException("站点还没有配置发件邮箱，暂时无法发送验证码");
        }

        Long userId = user.getId();
        String intervalKey = LoginVerificationConstant.LIMIT_KEY_PREFIX + userId;
        if (!redisService.setIfAbsent(
                intervalKey,
                "1",
                LoginVerificationConstant.SEND_INTERVAL_SECONDS)) {
            throw new LoginFailedException("验证码发送太频繁了，请稍后再试");
        }

        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String emailDailyKey = LoginVerificationConstant.EMAIL_DAILY_KEY_PREFIX
                + date + ":" + email;
        long emailCount = redisService.incrementAndExpire(
                emailDailyKey,
                1,
                LoginVerificationConstant.DAILY_COUNT_TTL_SECONDS
        );
        if (emailCount > LoginVerificationConstant.EMAIL_DAILY_LIMIT) {
            throw new LoginFailedException("该邮箱今日验证码发送次数已达上限");
        }

        String safeIp = StringUtils.hasText(ip) ? ip.trim() : "unknown";
        if (safeIp.length() > 100) {
            safeIp = safeIp.substring(0, 100);
        }
        String ipDailyKey = LoginVerificationConstant.IP_DAILY_KEY_PREFIX
                + date + ":" + safeIp;
        long ipCount = redisService.incrementAndExpire(
                ipDailyKey,
                1,
                LoginVerificationConstant.DAILY_COUNT_TTL_SECONDS
        );
        if (ipCount > LoginVerificationConstant.IP_DAILY_LIMIT) {
            throw new LoginFailedException("当前网络今日验证码发送次数已达上限");
        }

        String code = String.format("%06d", RANDOM.nextInt(1000000));
        String codeKey = LoginVerificationConstant.CODE_KEY_PREFIX + userId;
        String attemptKey = LoginVerificationConstant.ATTEMPT_KEY_PREFIX + userId;
        redisService.set(codeKey, code, LoginVerificationConstant.CODE_TTL_SECONDS);
        redisService.delete(attemptKey);

        try {
            mailService.sendHtml(
                    email,
                    "【小叶同学的博客】登录验证码",
                    buildLoginCodeHtml(code)
            );
        } catch (Exception e) {
            redisService.delete(codeKey);
            redisService.delete(attemptKey);
            redisService.delete(intervalKey);
            log.error("登录验证码发送失败，用户ID：{}", userId, e);
            throw new LoginFailedException("验证码发送失败，请稍后重试");
        }
    }

    @Override
    public SysUser verifyLoginCode(LoginVerifyDTO loginVerifyDTO) {
        LoginRiskTicketDTO ticketData = getRiskTicket(loginVerifyDTO.getRiskTicket());
        String requestDeviceId = trimToEmpty(loginVerifyDTO.getDeviceId());
        if (!requestDeviceId.equals(ticketData.getDeviceId())) {
            throw new LoginFailedException("登录设备与验证凭证不匹配，请重新登录");
        }

        SysUser user = userMapper.getByUserId(ticketData.getUserId());
        validateUser(user);
        Long userId = user.getId();
        String codeKey = LoginVerificationConstant.CODE_KEY_PREFIX + userId;
        String attemptKey = LoginVerificationConstant.ATTEMPT_KEY_PREFIX + userId;
        String cachedCode = redisService.get(codeKey, String.class);
        if (!StringUtils.hasText(cachedCode)) {
            throw new LoginFailedException("验证码已过期，请重新获取");
        }

        String inputCode = trimToEmpty(loginVerifyDTO.getEmailCode());
        if (!cachedCode.equals(inputCode)) {
            long attempts = redisService.incrementAndExpire(
                    attemptKey,
                    1,
                    LoginVerificationConstant.CODE_TTL_SECONDS
            );
            if (attempts >= LoginVerificationConstant.MAX_ATTEMPTS) {
                redisService.delete(codeKey);
                redisService.delete(attemptKey);
                throw new LoginFailedException("验证码错误次数过多，请重新获取");
            }
            throw new LoginFailedException("验证码不正确");
        }

        redisService.delete(codeKey);
        redisService.delete(attemptKey);
        redisService.delete(
                LoginVerificationConstant.RISK_KEY_PREFIX + loginVerifyDTO.getRiskTicket()
        );
        return user;
    }

    @Override
    public void recordPasswordFailure(Long userId) {
        if (userId == null) {
            return;
        }
        redisService.incrementAndExpire(
                LoginVerificationConstant.FAILURE_KEY_PREFIX + userId,
                1,
                LoginVerificationConstant.FAILURE_WINDOW_SECONDS
        );
    }

    @Override
    public void clearPasswordFailures(Long userId) {
        if (userId != null) {
            redisService.delete(LoginVerificationConstant.FAILURE_KEY_PREFIX + userId);
        }
    }

    @Override
    public void markDeviceTrusted(
            SysUser user, String deviceId, DeviceInfoVO deviceInfo, String userAgent) {
        if (user == null || !StringUtils.hasText(deviceId)) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        UserDevice userDevice = UserDevice.builder()
                .userId(user.getId())
                .deviceUuid(deviceId.trim())
                .ipAddress(deviceInfo == null ? null : deviceInfo.getIp())
                .browser(deviceInfo == null ? null : deviceInfo.getBrowser())
                .os(deviceInfo == null ? null : deviceInfo.getOs())
                .deviceType(deviceInfo == null ? null : deviceInfo.getDeviceType())
                .location(deviceInfo == null ? null : deviceInfo.getLocation())
                .userAgent(userAgent)
                .isTrust(MultiStatusConstant.ONE)
                .lastLoginTime(now)
                .deleteFlag(DelStatusConstant.ENABLE)
                .createTime(now)
                .updateTime(now)
                .build();
        userDeviceMapper.upsertTrustedDevice(userDevice);
    }

    @Override
    public void trustRegistrationDevice(
            String email, String deviceId, DeviceInfoVO deviceInfo, String userAgent) {
        if (!StringUtils.hasText(deviceId)) {
            return;
        }
        SysUser user = userMapper.selectByEmail(normalizeEmail(email));
        if (user == null) {
            return;
        }
        markDeviceTrusted(user, deviceId, deviceInfo, userAgent);
    }

    private LoginRiskTicketDTO getRiskTicket(String riskTicket) {
        String ticket = trimToEmpty(riskTicket);
        if (!StringUtils.hasText(ticket)) {
            throw new LoginFailedException("登录验证凭证不能为空");
        }
        LoginRiskTicketDTO ticketData = redisService.get(
                LoginVerificationConstant.RISK_KEY_PREFIX + ticket,
                LoginRiskTicketDTO.class
        );
        if (ticketData == null) {
            throw new LoginFailedException("登录验证已失效，请重新输入账号密码");
        }
        return ticketData;
    }

    private void validateUser(SysUser user) {
        if (user == null) {
            throw new LoginFailedException("用户不存在");
        }
        if (!MultiStatusConstant.ONE.equals(user.getStatus())) {
            throw new LoginFailedException("账号当前不可登录");
        }
        if (!DelStatusConstant.ENABLE.equals(user.getDeleteFlag())) {
            throw new LoginFailedException("账号当前不可登录");
        }
    }

    private String buildLoginCodeHtml(String code) {
        return "<div style=\"font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#333;line-height:1.9\">"
                + "<p>您好，</p>"
                + "<p>系统检测到本次登录需要安全验证，本次验证码为：</p>"
                + "<p style=\"font-size:26px;font-weight:bold;letter-spacing:6px;color:#4f46e5\">" + code + "</p>"
                + "<p>验证码 5 分钟内有效，请勿转发给他人。</p>"
                + "<p>如果这不是您本人的操作，请及时修改登录密码。</p>"
                + "</div>";
    }

    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 0) {
            return email;
        }
        String local = email.substring(0, at);
        String visible = local.length() <= 2
                ? local.substring(0, 1)
                : local.substring(0, 2);
        return visible + "****" + email.substring(at);
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
