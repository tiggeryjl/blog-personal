package com.blog.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 后台操作日志视图对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OperLogVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long operUserId;

    private String operUsername;

    private String operNickname;

    private String moduleName;

    private String operTitle;

    /**
     * 操作类型 1新增 2修改 3删除 4查询 5导出 6导入
     */
    private Integer operType;

    private String requestUrl;

    private String requestMethod;

    private String ipAddress;

    private String browser;

    private String os;

    private String location;

    private String operParam;

    private String operResult;

    private Long costTime;

    /**
     * 操作状态 1成功 0失败
     */
    private Integer status;

    private String errorMsg;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
