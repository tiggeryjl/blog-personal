package com.blog.pojo.vo;

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
public class UserPageVo implements Serializable {

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

    //头像
    private String avatar;

    //状态
    private Integer status;

    // 多角色列表，一对多关联查询
    private List<SysRoleSimpleVO> roleList;

    //注册时间
    private LocalDateTime createTime;

    //修改时间
    private LocalDateTime updateTime;
}
