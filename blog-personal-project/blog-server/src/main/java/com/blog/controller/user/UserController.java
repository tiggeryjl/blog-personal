package com.blog.controller.user;

import com.blog.context.BaseContext;
import com.blog.pojo.dto.PasswordEditDTO;
import com.blog.pojo.dto.UserDTO;
import com.blog.pojo.dto.UserLoginDTO;
import com.blog.pojo.dto.UserRegisterDTO;
import com.blog.pojo.entity.SysUser;
import com.blog.pojo.vo.LoginVO;
import com.blog.pojo.vo.UserSimpleVO;
import com.blog.result.Result;
import com.blog.service.JwtService;
import com.blog.service.UserService;
import com.blog.utils.AliyunAcsClient;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
    private AliyunAcsClient aliyunAcsClient;

    /**
     * 用户注册
     * @return
     */
    @PostMapping("/register")
    public Result register(@RequestBody UserRegisterDTO userRegisterDTO) {
        log.info("用户注册信息:{}", userRegisterDTO);
        userService.register(userRegisterDTO);
        return Result.success();
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
    public Result<LoginVO> login(@RequestBody UserLoginDTO userLoginDTO, HttpServletResponse resp) {
        log.info("用户登录信息:{}", userLoginDTO);

//        String captchaVerifyParam = userLoginDTO.getCaptchaVerifyParam();
//        log.info("滑块验证码校验信息:{}", captchaVerifyParam);
//        // 1. 校验验证码
//        if (captchaVerifyParam == null || !aliyunAcsClient.verifyCaptcha(captchaVerifyParam)) {
//            return Result.error(MessageConstant.CODE_VERIFICATION_FAILED);
//        }

        SysUser user = userService.login(userLoginDTO);
        //TODO 生成token并返回
        String token = jwtService.createToken(user.getId(),user.getUsername());
        String refreshToken  = jwtService.createRefreshToken(user.getId());
        jwtService.setUserRefreshCookie(refreshToken,resp);

        UserSimpleVO simpleVO = new UserSimpleVO();
        BeanUtils.copyProperties(user, simpleVO);
        LoginVO loginVO = new LoginVO();
        loginVO.setUser(simpleVO);
        loginVO.setToken(token);

        return Result.success(loginVO);
    }

    /**
     * 修改密码
     * @return
     */
    @PutMapping("/editPassword")
    public Result updatePwd(@RequestBody PasswordEditDTO passwordEditDTO){
        log.info("修改密码信息:{}", passwordEditDTO);
        passwordEditDTO.setUserId(BaseContext.getCurrentId());
        userService.updatePwd(passwordEditDTO);
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
    public Result update(@RequestBody UserDTO userDTO){
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

}
