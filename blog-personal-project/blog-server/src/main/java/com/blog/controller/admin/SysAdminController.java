package com.blog.controller.admin;

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
import com.blog.utils.AliyunAcsClient;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private JwtService jwtService;
    @Autowired
    private AliyunAcsClient aliyunAcsClient;

    /**
     * 无感刷新token
     * 前端打开页面/access过期自动调用，自动携带cookie，不用传参
     */
    @PostMapping("/refreshToken")
    public Result<Map<String,String>> refreshToken(HttpServletRequest req, HttpServletResponse resp) {
        // 从请求cookie读取refreshToken
        String refreshToken = null;
        Cookie[] cookies = req.getCookies();
        if(cookies != null) {
            for (Cookie cookie : cookies) {
                if("admin_refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }
        // 无refreshToken或已过期
        if(refreshToken == null || !jwtService.validateToken(refreshToken)) {
            return Result.error(401, "登录失效，请重新登录");
        }
        // 解析用户id
        Long userId = jwtService.getUserIdFromRefreshToken(refreshToken);
        UserSimpleVO user = adminService.getUserInfo(userId);
        if(user == null) {
            return Result.error(401, "用户不存在");
        }
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
    public Result<LoginVO> login(@RequestBody UserLoginDTO userLoginDTO, HttpServletResponse resp) {
        log.info("管理员登录信息:{}", userLoginDTO);

        SysUser user = adminService.login(userLoginDTO);

        //TODO 生成token并返回
        String token = jwtService.createToken(user.getId(),user.getUsername());
        String refreshToken  = jwtService.createRefreshToken(user.getId());
        jwtService.setAdminRefreshCookie(refreshToken,resp);

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
    @PreAuthorize("hasAuthority('sys:user:add')")
    @PostMapping("/add")
    public Result<RoleSelectVO> add(@RequestBody UserDTO userDTO){
        log.info("新增用户:{}",userDTO);
        adminService.add(userDTO);
        return Result.success();
    }

    /**
     * 修改密码
     * @return
     */
    @PutMapping("/editPassword")
    public Result updatePwd(@RequestBody PasswordEditDTO passwordEditDTO){
        log.info("修改密码信息:{}", passwordEditDTO);
        passwordEditDTO.setUserId(BaseContext.getCurrentId());
        adminService.updatePwd(passwordEditDTO);
        return Result.success();
    }

    /**
     * 修改用户信息
     * @param userDTO
     * @return
     */
    @PreAuthorize("hasAuthority('sys:user:edit')")
    @PutMapping("/update")
    public Result update(@RequestBody UserDTO userDTO){
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
    @PreAuthorize("hasAuthority('sys:user:edit')")
    @PutMapping("/{id}/status/{status}")
    public Result updateStatus(@PathVariable Long id, @PathVariable Integer status){
        log.info("启用禁用用户{}的状态{}",id,status);
        adminService.updateStatus(id,status);
        return Result.success();
    }

    /**
     * 获取用户角色集合
     * @return
     */
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
    @PreAuthorize("hasAuthority('sys:user:role')")
    @PutMapping("/updateRole")
    public Result updateRole(@RequestBody UserRoleAssignDTO userRoleAssignDTO){
        log.info("分配用户角色:{}", userRoleAssignDTO);
        adminService.updateRole(userRoleAssignDTO);
        return Result.success();
    }


    /**
     * 逻辑删除用户
     * @param ids
     * @return
     */
    @PreAuthorize("hasAuthority('sys:user:delete')")
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
    @PreAuthorize("hasAuthority('sys:recycleUser:list')")
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
    @PreAuthorize("hasAuthority('sys:recycleUser:recycle')")
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
    @PreAuthorize("hasAuthority('sys:recycleUser:delete')")
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
    public Result<String> logout(HttpServletResponse resp) {
        jwtService.clearAdminRefreshCookie(resp);
        return Result.success();
    }

}
