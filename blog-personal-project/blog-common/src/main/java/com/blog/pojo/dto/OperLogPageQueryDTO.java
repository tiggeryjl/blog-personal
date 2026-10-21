package com.blog.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志分页查询参数
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OperLogPageQueryDTO implements Serializable {

    /**
     * 关键字（操作人 / 操作标题）
     */
    private String keyword;

    /**
     * 操作模块
     */
    private String moduleName;

    /**
     * 操作类型 1新增 2修改 3删除 4查询 5导出 6导入
     */
    private Integer operType;

    /**
     * 操作状态 1成功 0失败
     */
    private Integer status;

    /**
     * 开始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime beginTime;

    /**
     * 结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 页码
     */
    @Builder.Default
    private Integer page = 1;

    /**
     * 每页条数
     */
    @Builder.Default
    private Integer pageSize = 10;
}
