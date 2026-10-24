package com.blog.pojo.dto;

import com.blog.validation.ValidationGroups;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO implements Serializable {

    private Long id;

    //昵称
    @Size(max = 50, message = "昵称不能超过50个字符")
    private String nickname;

    //用户名
    private String username;

    //手机号
    private String phone;

    //邮箱
    @NotBlank(groups = ValidationGroups.Create.class, message = "邮箱不能为空")
    @Size(max = 100, message = "邮箱不能超过100个字符")
    @Email(message = "邮箱格式错误")
    private String email;

    //性别 0=男 1=女 2=保密
    private Integer sex;

    //头像
    private String avatar;

    //个人简介
    @Size(max = 500, message = "个人简介不能超过500个字符")
    private String intro;

    // 个人网站
    private String website;

    // GitHub
    private String github;

    //注册时间
    private LocalDateTime createTime;

}
