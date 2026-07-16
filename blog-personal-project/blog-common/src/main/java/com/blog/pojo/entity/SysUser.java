package com.blog.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    //昵称
    private String nickname;

    //用户名
    private String username;

    //手机号
    private String phone;

    //邮箱
    private String email;

    //性别 0=男 1=女 2=保密
    private Integer sex;

    //密码
    private String password;

    //密码盐值
    private String salt;

    //头像
    private String avatar;

    //个人简介
    private String intro;

    //账号状态 0=正常 1=禁用 2=注销
    private Integer status;

    // 逻辑删除 0=正常 1=删除
    private Integer deleteFlag;

    // 锁标识 0=未锁定 1=已锁定
    private Integer lockFlag;

    // 锁定时间
    private LocalDateTime lockTime;

    // 个人网站
    private String website;

    // GitHub
    private String github;

    //注册时间
    private LocalDateTime createTime;

    //修改时间
    private LocalDateTime updateTime;

    // 最后登录IP
    private String loginIp;

    // 最后登录浏览器
    private String loginAgent;

    //最后登录时间
    private LocalDateTime loginTime;

    // 非数据库字段：用户绑定角色集合
    private List<SysRole> roleList;
}
