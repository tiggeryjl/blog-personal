package com.blog.service;

import com.blog.pojo.dto.DailyDTO;
import com.blog.pojo.dto.DailyPageQueryDTO;
import com.blog.pojo.entity.Daily;
import com.blog.result.PageResult;

import java.util.List;

/**
 * 日常 Service
 */
public interface DailyService {

    /**
     * 分页查询正常列表的日常
     * @param dailyPageQueryDTO 查询参数
     * @return 分页结果
     */
    PageResult pageQuery(DailyPageQueryDTO dailyPageQueryDTO);

    /**
     * 新增日常
     * @param dailyDTO 日常信息
     */
    void add(DailyDTO dailyDTO);

    /**
     * 根据ID查询日常
     * @param id 日常ID
     * @return 日常信息
     */
    Daily getById(Long id);

    /**
     * 修改日常
     * @param dailyDTO 日常信息
     */
    void update(DailyDTO dailyDTO);

    /**
     * 修改日常状态（发布/下架/私密等）
     * @param dailyDTO 日常ID与目标状态
     */
    void updateStatus(DailyDTO dailyDTO);

    /**
     * 切换置顶状态
     * @param id 日常ID
     */
    void updateTop(Long id);

    /**
     * 设置定时发布
     * @param dailyDTO 日常ID与定时时间
     */
    void setTimedPublish(DailyDTO dailyDTO);

    /**
     * 取消定时发布
     * @param id 日常ID
     */
    void cancelTimedPublish(Long id);

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
