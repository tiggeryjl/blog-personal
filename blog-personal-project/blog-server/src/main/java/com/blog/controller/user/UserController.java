package com.blog.controller.user;

import com.blog.constant.MessageConstant;
import com.blog.context.BaseContext;
import com.blog.pojo.dto.EmailRegisterDTO;
import com.blog.pojo.dto.PasswordEditDTO;
import com.blog.pojo.dto.ResetPasswordCodeDTO;
import com.blog.pojo.dto.ResetPasswordDTO;
import com.blog.pojo.dto.ResetPasswordVerifyDTO;
import com.blog.pojo.dto.RegisterCodeDTO;
import com.blog.pojo.dto.LoginCodeDTO;
import com.blog.pojo.dto.LoginVerifyDTO;
import com.blog.pojo.dto.UserDTO;
import com.blog.pojo.dto.UserLoginDTO;
import com.blog.pojo.dto.UserRegisterDTO;
import com.blog.pojo.entity.SysUser;
import com.blog.pojo.vo.DeviceInfoVO;
import com.blog.pojo.vo.LoginVO;
import com.blog.pojo.vo.LoginRiskInfoVO;
import com.blog.pojo.vo.UserPersonalInfoVO;
import com.blog.pojo.vo.UserSimpleVO;
import com.blog.result.Result;
import com.blog.service.JwtService;
import com.blog.service.LoginVerificationService;
import com.blog.service.UserService;
import com.blog.utils.AliyunAcsClient;
import com.blog.utils.DeviceInfoUtil;
import com.blog.utils.IpUtil;
import jakarta.validation.Valid;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/user/user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private LoginVerificationService loginVerificationService;
    @Autowired
    private AliyunAcsClient aliyunAcsClient;

    /**
     * 用户注册
     * @return
    */
    @PostMapping("/register")
    public Result register(@Valid @RequestBody UserRegisterDTO userRegisterDTO,
                           HttpServletRequest request) {
        log.info("用户注册，邮箱：{}", userRegisterDTO.getEmail());
        userService.register(userRegisterDTO);
        try {
            loginVerificationService.trustRegistrationDevice(
                    userRegisterDTO.getEmail(),
                    userRegisterDTO.getDeviceId(),
                    buildDeviceInfo(request),
                    request.getHeader("User-Agent")
            );
        } catch (Exception e) {
            // 注册已经成功，设备绑定失败不能让用户误以为账号没有创建
            log.warn("注册设备绑定失败，邮箱：{}", userRegisterDTO.getEmail(), e);
        }
        return Result.success();
    }

    /**
     * 发送注册邮箱验证码
     *
     * @param registerCodeDTO 注册邮箱
     * @return 统一结果
     */
    @PostMapping("/register/code")
    public Result sendRegisterCode(@Valid @RequestBody RegisterCodeDTO registerCodeDTO,
                                   HttpServletRequest request) {
        log.info("发送注册验证码，邮箱：{}", registerCodeDTO.getEmail());
        userService.sendRegisterCode(registerCodeDTO.getEmail(), IpUtil.getClientIp(request));
        return Result.success("验证码已发送");
    }

    /**
     * 判断邮箱是否已注册
     *
     * @param email 邮箱
     * @return true=已注册
     */
    @GetMapping("/emailExists")
    public Result<Boolean> emailExists(@RequestParam("email") String email) {
        return Result.success(userService.existsByEmail(email));
    }

    /**
     * 邮箱快捷注册
     *
     * @param emailRegisterDTO 邮箱、密码与确认密码
     * @return 统一结果
    */
    @PostMapping("/registerByEmail")
    public Result<LoginVO> registerByEmail(@Valid @RequestBody EmailRegisterDTO emailRegisterDTO,
                                           HttpServletRequest request,
                                           HttpServletResponse response) {
        log.info("邮箱快捷注册:{}", emailRegisterDTO.getEmail());
        SysUser user = userService.registerByEmail(emailRegisterDTO);
        try {
            loginVerificationService.markDeviceTrusted(
                    user,
                    emailRegisterDTO.getDeviceId(),
                    buildDeviceInfo(request),
                    request.getHeader("User-Agent")
            );
        } catch (Exception e) {
            log.warn("快捷注册设备绑定失败，邮箱：{}", emailRegisterDTO.getEmail(), e);
        }
        return Result.success(buildLoginVO(user, false, response));
    }

    /**
     * 刷新token
     * @param req
     * @param resp
     * @return
     */
    @PostMapping("/refreshToken")
    public Result<Map<String,String>> refreshToken(HttpServletRequest req, HttpServletResponse resp) {
        // 读取Cookie中的refreshToken
        String refreshToken = null;
        Cookie[] cookies = req.getCookies();
        if(cookies != null) {
            for (Cookie cookie : cookies) {
                if("user_refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }
        // !validateToken代表token失效/不存在返回401
        if(refreshToken == null || !jwtService.validateToken(refreshToken)) {
            return Result.error(401, "登录失效，请重新登录");
        }
        // 专用方法解析refreshToken的userId（从自定义claim取，不从subject UUID取）
        Long userId = jwtService.getUserIdFromRefreshToken(refreshToken);
        UserSimpleVO user = userService.getUserInfo(userId);
        if(user == null) {
            return Result.error(401, "用户不存在");
        }
        // 生成新短期accessToken
        String newAccessToken = jwtService.createToken(userId, user.getUsername());

        Map<String,String> res = new HashMap<>();
        res.put("token", newAccessToken); // 和前端localStorage存储字段对齐
        return Result.success(res);
    }

    /**
     * 用户登录
     * @param userLoginDTO
     * @return
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody UserLoginDTO userLoginDTO,
                                 HttpServletRequest request,
                                 HttpServletResponse resp) {
        log.info("用户登录");
        String captchaVerifyParam = userLoginDTO.getCaptchaVerifyParam();
        if (!StringUtils.hasText(captchaVerifyParam)
                || !aliyunAcsClient.verifyCaptcha(captchaVerifyParam)) {
            return Result.error(400, MessageConstant.CODE_VERIFICATION_FAILED);
        }
        SysUser user = userService.login(userLoginDTO);
        String ip = IpUtil.getClientIp(request);
        String riskReason = loginVerificationService.getRiskReason(
                user.getId(), user.getEmail(), userLoginDTO.getDeviceId());
        if (riskReason != null) {
            LoginRiskInfoVO riskInfo = loginVerificationService.createRiskTicket(
                    user, userLoginDTO.getDeviceId(), ip, riskReason);
            LoginVO loginVO = LoginVO.builder()
                    .needEmailCode(true)
                    .riskTicket(riskInfo.getRiskTicket())
                    .maskedEmail(riskInfo.getMaskedEmail())
                    .expiresIn(riskInfo.getExpiresIn())
                    .riskReason(riskInfo.getRiskReason())
                    .build();
            return Result.success(loginVO);
        }

        loginVerificationService.clearPasswordFailures(user.getId());
        boolean remember = Boolean.TRUE.equals(userLoginDTO.getRemember());
        return Result.success(buildLoginVO(user, remember, resp));
    }

    /**
     * 风险登录：发送邮箱验证码
     */
    @PostMapping("/login/code")
    public Result sendLoginCode(@Valid @RequestBody LoginCodeDTO loginCodeDTO,
                                HttpServletRequest request) {
        loginVerificationService.sendLoginCode(
                loginCodeDTO.getRiskTicket(), IpUtil.getClientIp(request));
        return Result.success("验证码已发送");
    }

    /**
     * 风险登录：验证邮箱验证码并签发登录凭证
     */
    @PostMapping("/login/verify")
    public Result<LoginVO> verifyLoginCode(@Valid @RequestBody LoginVerifyDTO loginVerifyDTO,
                                           HttpServletRequest request,
                                           HttpServletResponse response) {
        SysUser user = loginVerificationService.verifyLoginCode(loginVerifyDTO);
        loginVerificationService.clearPasswordFailures(user.getId());
        loginVerificationService.markDeviceTrusted(
                user,
                loginVerifyDTO.getDeviceId(),
                buildDeviceInfo(request),
                request.getHeader("User-Agent")
        );
        boolean remember = Boolean.TRUE.equals(loginVerifyDTO.getRemember());
        return Result.success(buildLoginVO(user, remember, response));
    }

    /**
     * 修改密码
     * @return
     */
    @PutMapping("/editPassword")
    public Result updatePwd(@Valid @RequestBody PasswordEditDTO passwordEditDTO){
        log.info("用户id:{}-修改密码", passwordEditDTO.getUserId());
        passwordEditDTO.setUserId(BaseContext.getCurrentId());
        userService.updatePwd(passwordEditDTO);
        return Result.success();
    }

    /**
     * 忘记密码：发送邮箱验证码
     *
     * @param resetPasswordCodeDTO 注册邮箱
     * @return 统一结果
     */
    @PostMapping("/resetPwd/code")
    public Result sendResetPwdCode(@Valid @RequestBody ResetPasswordCodeDTO resetPasswordCodeDTO) {
        log.info("发送重置密码验证码:{}", resetPasswordCodeDTO.getEmail());
        userService.sendResetPwdCode(resetPasswordCodeDTO.getEmail());
        return Result.success();
    }

    /**
     * 忘记密码：校验邮箱验证码，通过后返回一次性凭证
     *
     * @param resetPasswordVerifyDTO 邮箱与验证码
     * @return 重置密码用的一次性凭证
     */
    @PostMapping("/resetPwd/verify")
    public Result<String> verifyResetPwdCode(@Valid @RequestBody ResetPasswordVerifyDTO resetPasswordVerifyDTO) {
        log.info("校验重置密码验证码:{}", resetPasswordVerifyDTO.getEmail());
        return Result.success(userService.verifyResetPwdCode(resetPasswordVerifyDTO));
    }

    /**
     * 忘记密码：凭一次性凭证重置密码
     *
     * @param resetPasswordDTO 凭证与新密码
     * @return 统一结果
     */
    @PostMapping("/resetPwd")
    public Result resetPwdByTicket(@Valid @RequestBody ResetPasswordDTO resetPasswordDTO) {
        log.info("重置密码");
        userService.resetPwdByTicket(resetPasswordDTO);
        return Result.success();
    }

    /**
     * 查询用户
     * @return
     */
    @GetMapping("/getUserInfo")
    public Result<UserSimpleVO> getUserInfo(){
        log.info("获取用户信息");
        Long id = BaseContext.getCurrentId();
        UserSimpleVO userVo=userService.getUserInfo(id);
        return Result.success(userVo);
    }

    /**
     * 修改用户信息
     * @param userDTO
     * @return
     */
    @PutMapping("/update")
    public Result update(@Valid @RequestBody UserDTO userDTO){
        log.info("修改用户:{}", userDTO);
        userDTO.setId(BaseContext.getCurrentId());
        userService.update(userDTO);
        return Result.success();
    }

    /**
     * 用户退出
     * @return
     */
    @PostMapping("/logout")
    public Result<String> logout(HttpServletResponse resp) {
        jwtService.clearUserRefreshCookie(resp);
        return Result.success();
    }

    /**
     * 获取个人信息
     * @return
     */
    @GetMapping("/personalInfo")
    public Result<UserPersonalInfoVO> getPersonalInfo(){
        log.info("获取个人信息");
        UserPersonalInfoVO userVo=userService.getPersonalInfo();
        return Result.success(userVo);
    }

    /**
     * 签发用户登录凭证
     */
    private LoginVO buildLoginVO(SysUser user, boolean remember, HttpServletResponse response) {
        String token = jwtService.createToken(user.getId(), user.getUsername());
        String refreshToken = jwtService.createRefreshToken(user.getId(), remember);
        jwtService.setUserRefreshCookie(refreshToken, remember, response);

        UserSimpleVO simpleVO = new UserSimpleVO();
        BeanUtils.copyProperties(user, simpleVO);
        return LoginVO.builder()
                .needEmailCode(false)
                .user(simpleVO)
                .token(token)
                .build();
    }

    /**
     * 获取当前登录设备信息
     */
    private DeviceInfoVO buildDeviceInfo(HttpServletRequest request) {
        return DeviceInfoVO.builder()
                .ip(IpUtil.getClientIp(request))
                .browser(DeviceInfoUtil.getBrowserInfo(request))
                .os(DeviceInfoUtil.getOsInfo(request))
                .deviceType(DeviceInfoUtil.getDeviceType(request))
                .build();
    }

}
