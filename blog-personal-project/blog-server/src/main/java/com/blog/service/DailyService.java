package com.blog.service;

import com.blog.pojo.dto.DailyPageQueryDTO;
import com.blog.result.PageResult;

import java.util.List;

/**
 * 日常 Service
 */
public interface DailyService {

    /**
     * 分页查询逻辑删除的日常（回收站）
     * @param dailyPageQueryDTO 查询参数
     * @return 分页结果
     */
    PageResult recyclePageQuery(DailyPageQueryDTO dailyPageQueryDTO);

    /**
     * 批量恢复（回收站 -> 正常列表）
     * @param ids 日常ID集合
     */
    void recover(List<Long> ids);

    /**
     * 批量逻辑删除
     * @param ids 日常ID集合
     */
    void logicDelete(List<Long> ids);

    /**
     * 批量彻底删除
     * @param ids 日常ID集合
     */
    void delete(List<Long> ids);
}
