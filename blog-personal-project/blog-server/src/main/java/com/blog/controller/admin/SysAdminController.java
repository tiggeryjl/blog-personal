package com.blog.controller.admin;

import com.blog.constant.MultiStatusConstant;
import com.blog.mapper.SysUserMapper;
import com.blog.properties.JwtProperties;
import com.blog.service.RedisService;
import com.blog.validation.ValidationGroups;
import com.blog.context.BaseContext;
import com.blog.pojo.dto.*;
import com.blog.pojo.entity.LoginUser;
import com.blog.pojo.entity.SysUser;
import com.blog.pojo.vo.LoginVO;
import com.blog.pojo.vo.RoleSelectVO;
import com.blog.pojo.vo.UserInfoVO;
import com.blog.pojo.vo.UserSimpleVO;
import com.blog.result.PageResult;
import com.blog.result.Result;
import com.blog.service.JwtService;
import com.blog.service.SysAdminService;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import com.blog.utils.AliyunAcsClient;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/admin/admin")
public class SysAdminController {

    @Autowired
    private SysAdminService adminService;
    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private AliyunAcsClient aliyunAcsClient;
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private RedisService redisService;

    private String readCookie(HttpServletRequest req, String name) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }

    /**
     * 无感刷新token
     * 前端打开页面/access过期自动调用，自动携带cookie，不用传参
     */
    @PostMapping("/refreshToken")
    public Result<Map<String,String>> refreshToken(HttpServletRequest req, HttpServletResponse resp) {
        // 从请求cookie读取refreshToken
        String refreshToken = readCookie(req, "admin_refreshToken");

        // 无refreshToken或已过期
        if(refreshToken == null || !jwtService.validateToken(refreshToken)) {
            return Result.error(401, "登录失效，请重新登录");
        }
        // 解析用户id
        Long userId = jwtService.getUserIdFromRefreshToken(refreshToken);
//        UserSimpleVO user = adminService.getUserInfo(userId);
        SysUser user = userMapper.getByUserId(userId);
        if(user == null) {
            return Result.error(401, "用户不存在");
        }

        if (!MultiStatusConstant.ONE.equals(user.getStatus())
                || Integer.valueOf(1).equals(user.getDeleteFlag())) {
            return Result.error(401, "账号已被禁用或注销");
        }

        //比对 Redis 白名单，旧 rt 已登出/改密即拒绝
        String rtId = jwtService.getRtId(refreshToken);
        Object savedRtId = redisService.get("rt:admin:" + userId, String.class);
        if (savedRtId == null || !savedRtId.equals(rtId)) {
            return Result.error(401, "登录已失效，请重新登录");
        }

        // 轮换：签发新 rt 覆盖旧 rt
        String newRt = jwtService.createRefreshToken(userId, true);
        jwtService.setUserRefreshCookie(newRt, true, resp);
        redisService.set("rt:admin:" + userId, jwtService.getRtId(newRt),
                jwtProperties.getUserRefreshTtl() / 1000);

        // 生成新accessToken
        String newAccessToken = jwtService.createToken(userId, user.getUsername());

        Map<String,String> res = new HashMap<>();
        res.put("token", newAccessToken);
        return Result.success(res);
    }

    /**
     * 管理员登录
     * @param userLoginDTO
     * @return
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody UserLoginDTO userLoginDTO, HttpServletResponse resp) {
        log.info("管理员登录信息:{}", userLoginDTO);

        SysUser user = adminService.login(userLoginDTO);

        //生成token并返回
        boolean remember = Boolean.TRUE.equals(userLoginDTO.getRemember());
        String token = jwtService.createToken(user.getId(),user.getUsername());
        String refreshToken  = jwtService.createRefreshToken(user.getId(), remember);
        jwtService.setAdminRefreshCookie(refreshToken, remember, resp);

        // rt 白名单：key=rt:user:{userId}，value=rtId，TTL 与 rt 一致
        long ttlSeconds = remember
                ? jwtProperties.getUserRefreshTtl() / 1000
                : 12 * 60 * 60;   // 未勾选记住我：12 小时
        redisService.set("rt:admin:" + user.getId(), jwtService.getRtId(refreshToken), ttlSeconds);

        UserSimpleVO userSimpleVO = new UserSimpleVO();
        BeanUtils.copyProperties(user, userSimpleVO);

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUser(userSimpleVO);

        return Result.success(loginVO);
    }

    /**
     * 查询用户及角色权限信息
     * @return
     */
    @GetMapping("/getUserInfo")
    public Result<UserInfoVO> getUserInfo() {
        log.info("获取用户及角色权限信息");
        // 获取当前登录用户
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = loginUser.getUsername();
        UserInfoVO userInfo = adminService.getUserInfoByUsername(username);
        return Result.success(userInfo);
    }

    /**
     * 查询用户
     * @return
     */
    @PreAuthorize("hasPermission(null,'sys:user:list')")
    @GetMapping("/getUserList")
    public Result<PageResult> getUserList(UserPageQueryDTO param) {
        log.info("获取用户信息{}", param);
        PageResult page = adminService.pageQurey(param);
        return Result.success(page);
    }

    /**
     * 新增用户
     * @return
     */
    @PreAuthorize("hasPermission(null,'sys:user:add')")
    @PostMapping("/add")
    public Result<RoleSelectVO> add(@Valid @RequestBody UserDTO userDTO){
        log.info("新增用户:{}",userDTO);
        adminService.add(userDTO);
        return Result.success();
    }

    /**
     * 修改密码
     * @return
     */
    @PutMapping("/editPassword")
    public Result updatePwd(@Valid @RequestBody PasswordEditDTO passwordEditDTO){
        log.info("管理员id:{}-修改密码", passwordEditDTO.getUserId());
        passwordEditDTO.setUserId(BaseContext.getCurrentId());
        adminService.updatePwd(passwordEditDTO);
        redisService.delete("rt:admin:" + passwordEditDTO.getUserId());
        return Result.success();
    }

    /**
     * 重置用户密码（生成随机密码返回给管理员转告用户）
     *
     * @param id 用户ID
     * @return 重置后的新密码
     */
    @PreAuthorize("hasPermission(null,'sys:user:reset')")
    @PutMapping("/{id}/resetPassword")
    public Result<String> resetPassword(@PathVariable Long id) {
        log.info("重置用户密码:{}", id);
        String resetPassword = adminService.resetPassword(id);
        redisService.delete("rt:admin:" +id);
        return Result.success(resetPassword);
    }

    /**
     * 修改用户信息
     * @param userDTO
     * @return
     */
    @PreAuthorize("hasPermission(null,'sys:user:edit')")
    @PutMapping("/update")
    public Result update(@Valid @RequestBody UserDTO userDTO){
        log.info("修改用户:{}", userDTO);
        adminService.update(userDTO);
        return Result.success();
    }

    /**
     * 修改用户状态
     * @param id
     * @param status
     * @return
     */
    @PreAuthorize("hasPermission(null,'sys:user:edit')")
    @PutMapping("/{id}/status/{status}")
    public Result updateStatus(@PathVariable Long id, @PathVariable Integer status){
        log.info("启用禁用用户{}的状态{}",id,status);
        adminService.updateStatus(id,status);
        redisService.delete("rt:admin:" +id);
        return Result.success();
    }

    /**
     * 获取用户角色集合
     * @return
     */
    @PreAuthorize("hasPermission(null,'sys:user:add') or hasPermission(null,'sys:user:role')")
    @GetMapping("/getRoleList")
    public Result<List<RoleSelectVO>> getRoleList(){
        log.info("获取用户角色集合");
        List<RoleSelectVO> roleVOList = adminService.getRoleList();
        return Result.success(roleVOList);
    }

    /**
     * 分配用户角色
     * @return
     */
    @PreAuthorize("hasPermission(null,'sys:user:role')")
    @PutMapping("/updateRole")
    public Result updateRole(@Valid @RequestBody UserRoleAssignDTO userRoleAssignDTO){
        log.info("分配用户角色:{}", userRoleAssignDTO);
        adminService.updateRole(userRoleAssignDTO);
        return Result.success();
    }


    /**
     * 逻辑删除用户
     * @param ids
     * @return
     */
    @PreAuthorize("hasPermission(null,'sys:user:delete')")
    @DeleteMapping("/logicDelete")
    public Result logicDelete(@RequestParam List<Long> ids){
        log.info("逻辑删除用户的ids{}",ids);
        adminService.logicDelete(ids);
        return Result.success();
    }

    /**
     * 分页查询逻辑删除的用户
     * @param param
     * @return
     */
    @PreAuthorize("hasPermission(null,'sys:recycleUser:list')")
    @GetMapping("/getLogicDelete")
    public Result<PageResult> getLogicDelete(UserPageQueryDTO param) {
        log.info("获取逻辑删除用户信息{}", param);
        PageResult page = adminService.pageQueryLogicDelete(param);
        return Result.success(page);
    }

    /**
     * 恢复用户
     * @param id
     * @return
     */
    @PreAuthorize("hasPermission(null,'sys:recycleUser:recycle')")
    @PutMapping("/recover")
    public Result recover(@RequestParam Long id) {
        log.info("恢复用户的id{}", id);
        adminService.recover(id);
        return Result.success();
    }

    /**
     * 彻底删除用户
     * @param ids
     * @return
     */
    @PreAuthorize("hasPermission(null,'sys:recycleUser:delete')")
    @DeleteMapping()
    public Result delete(@RequestParam List<Long> ids){
        log.info("彻底删除用户的ids{}",ids);
        adminService.delete(ids);
        return Result.success();
    }

    /**
     * 用户退出
     * @return
     */
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest req,HttpServletResponse resp) {
        String rt = readCookie(req, "admin_refreshToken");
        if (rt != null && jwtService.validateToken(rt)) {
            Long userId = jwtService.getUserIdFromRefreshToken(rt);
            redisService.delete("rt:admin:" + userId);
        }
        jwtService.clearAdminRefreshCookie(resp);
        return Result.success();
    }

}
