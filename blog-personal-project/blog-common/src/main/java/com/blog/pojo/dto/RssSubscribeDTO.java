package com.blog.pojo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户端 RSS 邮箱订阅请求
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RssSubscribeDTO implements Serializable {

    //订阅邮箱
    @NotBlank(message = "订阅邮箱不能为空")
    @Email(message = "邮箱格式错误")
    @Size(max = 100, message = "邮箱不能超过100个字符")
    private String email;

    //昵称，可不填
    @Size(max = 50, message = "昵称不能超过50个字符")
    private String nickname;
}
