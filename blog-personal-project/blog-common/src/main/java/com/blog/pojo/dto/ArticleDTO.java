package com.blog.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArticleDTO implements Serializable {

    private Long id;

    //标题
    private String title;

    //摘要
    private String summary;

    //内容
    private String content;

    //分类id
    private Long categoryId;

    //标签id
    private List<Long> tags;

    //封面
    private String cover;

    //文章状态 0草稿 1已发布 2已下架 3已归档 4定时发布 5私密
    private Integer status;

    //创建时间
    private LocalDateTime createTime;

    //修改时间
    private LocalDateTime updateTime;

    //发布时间
    private LocalDateTime publishTime;

    //定时时间
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timedPublishTime;

    //用户id
    private Long userId;

    // 用户昵称
    private String userNickname;

    // 用户头像
    private String userAvatar;

    // 逻辑删除 0=正常 1=删除
    private Integer deleteFlag;

    // 排序
    private Integer sort;
}
