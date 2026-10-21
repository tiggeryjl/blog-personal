package com.blog.constant;

/**
 * 操作日志相关常量
 */
public final class OperLogConstant {

    /**
     * 操作类型：自动推断
     */
    public static final int TYPE_AUTO = 0;

    /**
     * 操作类型：新增
     */
    public static final int TYPE_ADD = 1;

    /**
     * 操作类型：修改
     */
    public static final int TYPE_UPDATE = 2;

    /**
     * 操作类型：删除
     */
    public static final int TYPE_DELETE = 3;

    /**
     * 操作类型：查询
     */
    public static final int TYPE_QUERY = 4;

    /**
     * 操作类型：导出
     */
    public static final int TYPE_EXPORT = 5;

    /**
     * 操作类型：导入
     */
    public static final int TYPE_IMPORT = 6;

    /**
     * 日志文本字段最大长度
     */
    public static final int MAX_TEXT_LENGTH = 2000;

    /**
     * 请求地址最大长度
     */
    public static final int MAX_URL_LENGTH = 500;

    /**
     * 日志保留天数，超期由定时任务清理
     */
    public static final int RETENTION_DAYS = 90;

}
