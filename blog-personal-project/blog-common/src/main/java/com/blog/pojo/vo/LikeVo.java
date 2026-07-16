package com.blog.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 点赞数VO实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LikeVo {
    /**
     * true已点赞，false取消点赞
     */
    private boolean isLike;

    /**
     * 记录点赞树
     */
    private Integer likeCount;
}
