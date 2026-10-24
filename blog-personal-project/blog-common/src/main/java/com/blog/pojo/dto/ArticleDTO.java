package com.blog.pojo.dto;

import com.blog.validation.ValidationGroups;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class ArticleDTO implements Serializable {

    @NotNull(groups = {
            ValidationGroups.Update.class,
            ValidationGroups.Status.class,
            ValidationGroups.Timed.class
    }, message = "文章ID不能为空")
    private Long id;

    //标题
    @NotBlank(message = "文章标题不能为空")
    @Size(max = 200, message = "文章标题不能超过200个字符")
    private String title;

    //摘要
    @Size(max = 500, message = "文章摘要不能超过500个字符")
    private String summary;

    //内容
    @NotBlank(message = "文章内容不能为空")
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
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
    @Min(value = 0, message = "排序不能小于0")
    private Integer sort;
}
