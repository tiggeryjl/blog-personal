package com.blog.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 后台操作日志实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SysOperLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志主键
     */
    private Long id;

    /**
     * 操作人用户ID
     */
    private Long operUserId;

    /**
     * 操作人用户名
     */
    private String operUsername;

    /**
     * 操作人昵称
     */
    private String operNickname;

    /**
     * 操作模块
     */
    private String moduleName;

    /**
     * 操作标题
     */
    private String operTitle;

    /**
     * 操作类型 1新增 2修改 3删除 4查询 5导出 6导入
     */
    private Integer operType;

    /**
     * 请求接口地址
     */
    private String requestUrl;

    /**
     * 请求方式
     */
    private String requestMethod;

    /**
     * 操作IP
     */
    private String ipAddress;

    /**
     * 浏览器
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 操作地点
     */
    private String location;

    /**
     * 请求入参JSON
     */
    private String operParam;

    /**
     * 返回结果JSON
     */
    private String operResult;

    /**
     * 接口耗时（毫秒）
     */
    private Long costTime;

    /**
     * 操作状态 1成功 0失败
     */
    private Integer status;

    /**
     * 失败异常信息
     */
    private String errorMsg;

    /**
     * 逻辑删除 0正常 1删除
     */
    private Integer deleteFlag;

    /**
     * 操作时间
     */
    private LocalDateTime createTime;
}
