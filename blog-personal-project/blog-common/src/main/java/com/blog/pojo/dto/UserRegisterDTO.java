package com.blog.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @NotBlank
 *   只能用在字符串 String
 *   不能为 null、不能是空字符串、不能全是空格,""、" " 全部拦截报错
 *   在controller接口上用了 @NotBlank，接口上要加 @Valid 才会生效
 * @NotNull
 *   可以用在任何类型（Integer、Long、对象、字符串）
 *   只校验：不能为 null,允许空字符串 ""、允许空格
 * @NotEmpty
 *   用在字符串 / 集合 / 数组
 *   不能为 null、不能长度为 0,但允许 " " 空格
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterDTO implements Serializable {

    //昵称
    private String nickname;

    //用户名
    @NotBlank(message = "用户名不能为空")
    private String username;

    //手机号
    @NotBlank(message = "手机号不能为空")
    private String phone;

    //邮箱
    @NotBlank(message = "邮箱不能为空")
    private String email;

    //密码
    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPwd;

    //邮箱验证码
    private String emailCode;

}
