package com.blog.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户端日常展示VO
 * 只返回用户端展示需要的公开字段
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DailyFrontVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日常ID
     */
    private Long id;

    /**
     * 日常内容
     */
    private String content;

    /**
     * 图片地址列表
     */
    private List<String> images;

    /**
     * 附件地址列表
     */
    private List<String> files;

    /**
     * 日常类型 0纯文字 1图片 2文件 3图文混合
     */
    private Integer type;

    /**
     * 作者昵称
     */
    private String userNickname;

    /**
     * 作者头像
     */
    private String userAvatar;

    /**
     * 是否置顶 0否 1是
     */
    private Integer isTop;

    /**
     * 点赞数
     */
    private Integer likeNum;

    /**
     * 评论数
     */
    private Integer commentNum;

    /**
     * 浏览数
     */
    private Integer viewNum;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;
}
