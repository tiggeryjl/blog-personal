package com.blog.service;

import com.blog.pojo.dto.OperLogPageQueryDTO;
import com.blog.pojo.entity.SysOperLog;
import com.blog.pojo.vo.OperLogVO;
import com.blog.result.PageResult;

import java.util.List;

/**
 * 后台操作日志服务
 */
public interface OperLogService {

    /**
     * 异步写入一条操作日志
     *
     * @param operLog 日志实体
     */
    void save(SysOperLog operLog);

    /**
     * 分页查询操作日志
     *
     * @param dto 查询参数
     * @return 分页结果
     */
    PageResult pageQuery(OperLogPageQueryDTO dto);

    /**
     * 查询日志详情
     *
     * @param id 日志ID
     * @return 日志详情
     */
    OperLogVO getById(Long id);

    /**
     * 批量删除日志
     *
     * @param ids 日志ID集合
     */
    void delete(List<Long> ids);

    /**
     * 清空全部日志（仅超级管理员）
     */
    void clear();

    /**
     * 删除超过保留天数的日志
     *
     * @return 删除条数
     */
    int cleanExpired();
}
