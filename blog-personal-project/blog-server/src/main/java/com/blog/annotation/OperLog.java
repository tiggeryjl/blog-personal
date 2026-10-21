package com.blog.annotation;

import com.blog.constant.OperLogConstant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记需要记录操作日志的接口
 * 不标注时由切面根据方法名和请求方式自动推断
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperLog {

    /**
     * 操作模块
     */
    String module() default "";

    /**
     * 操作标题
     */
    String title() default "";

    /**
     * 操作类型 1新增 2修改 3删除 4查询 5导出 6导入，0 表示自动推断
     */
    int type() default OperLogConstant.TYPE_AUTO;
}
