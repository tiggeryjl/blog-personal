package com.blog.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户点赞记录表
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserLike {
    private Long id;
    private Long userId;
    private Integer targetType;
    private Long targetId;
    private Integer deleteFlag;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
