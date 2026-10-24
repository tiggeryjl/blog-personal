package com.blog.pojo.dto;

import com.blog.validation.ValidationGroups;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 日常新增/修改/状态操作参数
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DailyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @NotNull(groups = {
            ValidationGroups.Update.class,
            ValidationGroups.Status.class,
            ValidationGroups.Timed.class
    }, message = "日常ID不能为空")
    private Long id;

    /**
     * 日常内容
     */
    @NotBlank(message = "日常内容不能为空")
    private String content;

    /**
     * 图片地址列表
     */
    private List<String> images;

    /**
     * 文件地址列表
     */
    private List<String> files;

    /**
     * 类型 0纯文字 1图片 2文件 3图文混合（为空时根据图片/文件自动计算）
     */
    private Integer type;

    /**
     * 是否置顶 0否 1是
     */
    @NotBlank(message = "置顶状态不合法")
    private Integer isTop;

    /**
     * 状态 0草稿 1已发布 2已下架 3定时发布 4私密
     */
    private Integer status;

    /**
     * 发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime publishTime;

    /**
     * 定时发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime timedPublishTime;

    /**
     * 排序权重
     */
    @Min(value = 0, message = "排序不能小于0")
    private Integer sort;
}
