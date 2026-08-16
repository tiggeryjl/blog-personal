package com.blog.handler;

import com.blog.constant.MessageConstant;
import com.blog.exception.BaseException;
import com.blog.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(500,ex.getMessage());
    }

    /**
     * 捕获运行时业务异常
     */
    @ExceptionHandler(RuntimeException.class)
    public Result runtimeExceptionHandler(RuntimeException ex) {
        log.error("运行时异常：{}", ex.getMessage(), ex);
        return Result.error(ex.getMessage());
    }

    /**
     * 处理SQL异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex){
        //Duplicate entry 'yesir' for key 'employee.idx_username'
        log.error("异常信息：{}", ex.getMessage());
        String message = ex.getMessage();
        if (message.contains("Duplicate entry")){
            String[] split = message.split(" ");
            String username =split[2];
            String msg=username+ MessageConstant.ACCOUNT_EXISTS;
            return Result.error(msg);
        }else {
            return Result.error(MessageConstant.UNKNOWN_ERROR);
        }
    }

    /**
     * 处理唯一键重复异常（MyBatis包装后的主键/唯一索引冲突）
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result duplicateKeyExceptionHandler(DuplicateKeyException ex) {
        log.error("唯一索引重复异常：{}", ex.getMessage());
        // 获取内层原始SQL异常
        Throwable cause = ex.getRootCause();
        if (cause instanceof SQLIntegrityConstraintViolationException) {
            String message = cause.getMessage();
            if (message.contains("Duplicate entry")) {
                String[] split = message.split(" ");
                String value = split[2];
                // 通用提示，也可以根据唯一索引名区分角色编码/用户名重复
                return Result.error("角色标识【" + value + "】已存在，请勿重复添加");
            }
        }
        return Result.error(MessageConstant.UNKNOWN_ERROR);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Result<?> accessDenied() {
        return Result.error(403, "权限不足");
    }

    @ExceptionHandler(AuthenticationException.class)
    public Result<?> authError() {
        return Result.error(401, "认证失败，请重新登录");
    }

    /**
     * 参数校验异常
     *
     * @param ex 参数校验异常
     * @return 统一结果
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> methodArgumentNotValid(MethodArgumentNotValidException ex) {
        log.error("参数校验失败：{}", ex.getMessage());
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getDefaultMessage())
                .orElse("参数校验失败");
        return Result.error(400, msg);
    }


    @ExceptionHandler(Exception.class)
    public Result<?> globalException(Exception e) {
        log.error("系统未知异常", e);
        return Result.error(500, "服务器异常");
    }

}

