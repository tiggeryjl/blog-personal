package com.blog.pojo.dto;

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
    private String email;

    //昵称，可不填
    private String nickname;
}
