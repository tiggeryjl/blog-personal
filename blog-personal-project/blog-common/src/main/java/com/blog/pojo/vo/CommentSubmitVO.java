package com.blog.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 评论提交结果
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentSubmitVO implements Serializable {

    private Long id;

    private Integer status;

    private Boolean needAudit;
}
