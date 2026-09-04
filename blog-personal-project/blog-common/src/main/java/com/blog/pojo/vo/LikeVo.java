package com.blog.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 点赞结果VO实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LikeVo {
    /**
     * 当前用户是否已点赞
     */
    private boolean liked;

    /**
     * 点赞总数
     */
    private Integer likeCount;
}
