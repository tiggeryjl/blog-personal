package com.blog.pojo.vo;

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
public class UserPersonalInfoVO implements Serializable {

    private Long id;

    //昵称
    private String nickname;

    //邮箱
    private String email;

    //头像
    private String avatar;

    //个人简介
    private String intro;

    // 个人网站
    private String website;

    // GitHub
    private String github;
}
