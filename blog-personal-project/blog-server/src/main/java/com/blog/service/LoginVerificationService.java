package com.blog.service;

import com.blog.pojo.dto.LoginVerifyDTO;
import com.blog.pojo.entity.SysUser;
import com.blog.pojo.vo.DeviceInfoVO;
import com.blog.pojo.vo.LoginRiskInfoVO;

/**
 * 登录风险评估与邮箱验证码服务
 */
public interface LoginVerificationService {

    /**
     * 判断登录是否需要邮箱验证
     *
     * @param userId 用户ID
     * @param email 用户邮箱
     * @param deviceId 浏览器设备标识
     * @return 需要验证时返回原因，正常登录返回 null
     */
    String getRiskReason(Long userId, String email, String deviceId);

    /**
     * 创建登录风险凭证
     */
    LoginRiskInfoVO createRiskTicket(SysUser user, String deviceId, String ip, String riskReason);

    /**
     * 发送登录邮箱验证码
     */
    void sendLoginCode(String riskTicket, String ip);

    /**
     * 校验登录邮箱验证码
     *
     * @return 验证通过的用户
     */
    SysUser verifyLoginCode(LoginVerifyDTO loginVerifyDTO);

    /**
     * 记录一次密码错误
     */
    void recordPasswordFailure(Long userId);

    /**
     * 清除密码错误次数
     */
    void clearPasswordFailures(Long userId);

    /**
     * 将当前设备标记为可信设备
     */
    void markDeviceTrusted(SysUser user, String deviceId, DeviceInfoVO deviceInfo, String userAgent);

    /**
     * 注册成功后信任当前注册设备
     */
    void trustRegistrationDevice(
            String email, String deviceId, DeviceInfoVO deviceInfo, String userAgent);
}
