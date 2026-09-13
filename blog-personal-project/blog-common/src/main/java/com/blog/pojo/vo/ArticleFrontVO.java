package com.blog.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户端文章列表VO
 * 只保留用户端列表真正展示的字段，正文、发布时间、定时发布时间、逻辑删除标识等不下发
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArticleFrontVO implements Serializable {

    private Long id;

    //标题
    private String title;

    //摘要
    private String summary;

    //封面
    private String cover;

    //分类名称
    private String category;

    //文章状态 1已发布 3已归档
    private Integer status;

    //是否置顶
    private Integer isTop;

    //是否热门
    private Integer isHot;

    //阅读量
    private Long viewNum;

    //点赞数
    private Long likeNum;

    //评论数
    private Long commentNum;

    //字数
    private Long wordsNum;

    //创建时间
    private LocalDateTime createTime;

    //修改时间
    private LocalDateTime updateTime;

    //发布时间
    private LocalDateTime publishTime;

    //作者昵称
    private String userNickname;

    //作者头像
    private String userAvatar;

    // 排序
    private Integer sort;

    //冗余标签
    private List<Long> tags;

    private List<String> tagNames;
}
