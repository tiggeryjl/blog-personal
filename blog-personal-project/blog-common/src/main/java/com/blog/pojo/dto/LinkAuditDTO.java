package com.blog.pojo.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 友链审核参数
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LinkAuditDTO implements Serializable {

    /**
     * 待审核友链ID集合
     */
    @NotEmpty(message = "请选择要审核的友链")
    private List<Long> ids;

    /**
     * 目标审核状态 1审核通过 2审核拒绝
     */
    @NotNull(message = "审核状态不能为空")
    private Integer auditStatus;
}
