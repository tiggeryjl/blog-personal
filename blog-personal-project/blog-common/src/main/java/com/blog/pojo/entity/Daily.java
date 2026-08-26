package com.blog.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 日常实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Daily implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 日常内容
     */
    private String content;

    /**
     * 图片地址，多个用英文逗号分隔
     */
    private String images;

    /**
     * 文件地址，多个用英文逗号分隔
     */
    private String files;

    /**
     * 发布者ID
     */
    private Long userId;

    /**
     * 作者昵称
     */
    private String userNickname;

    /**
     * 作者头像
     */
    private String userAvatar;

    /**
     * 类型 0纯文字 1图片 2文件 3图文混合
     */
    private Integer type;

    /**
     * 是否置顶 0否 1是
     */
    private Integer isTop;

    /**
     * 状态 0草稿 1已发布 2已下架 3定时发布 4私密
     */
    private Integer status;

    /**
     * 逻辑删除 0=正常 1=删除
     */
    private Integer deleteFlag;

    /**
     * 排序权重
     */
    private Integer sort;

    /**
     * 点赞数
     */
    private Integer likeNum;

    /**
     * 评论数
     */
    private Integer commentNum;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;

    /**
     * 定时发布时间
     */
    private LocalDateTime timedPublishTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;
}
