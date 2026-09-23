package com.blog.pojo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码必须大于0")
    @Builder.Default
    private Integer page = 1;

    /**
     * 每页显示的记录数
     */
    @NotNull(message = "每页显示条数不能为空")
    @Min(value = 1, message = "每页显示条数必须大于0")
    @Max(value = 100, message = "每页显示条数不能超过100")
    @Builder.Default
    private Integer pageSize = 10;
}
