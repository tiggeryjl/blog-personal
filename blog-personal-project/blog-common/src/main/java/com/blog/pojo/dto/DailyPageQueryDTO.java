package com.blog.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 日常分页查询参数（普通列表与回收站共用）
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DailyPageQueryDTO implements Serializable {

    /**
     * 内容关键词
     */
    private String content;

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
     * 开始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime begin;

    /**
     * 结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime end;

    /**
     * 页码
     */
    @Builder.Default
    private Integer page = 1;

    /**
     * 每页显示的记录数
     */
    @Builder.Default
    private Integer pageSize = 10;
}
