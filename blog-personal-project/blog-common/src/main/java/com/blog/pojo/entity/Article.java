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
public class Article implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    //标题
    private String title;

    //摘要
    private String summary;

    //内容
    private String content;

    //分类Id
    private Long categoryId;

    //封面
    private String cover;

    //文章状态 0草稿 1已发布 2已下架 3已归档 4定时发布 5私密
    private Integer status;

    //置顶
    private Integer isTop;

    //热门
    private Integer isHot;

    //阅读量
    private Long viewNum;

    //点赞数
    private Long likeNum;

    //评论数
    private Long commentNum;

    //文字数
    private Long wordsNum;

    //创建时间
    private LocalDateTime createTime;

    //修改时间
    private LocalDateTime updateTime;

    //发布时间
    private LocalDateTime publishTime;

    //定时时间
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
