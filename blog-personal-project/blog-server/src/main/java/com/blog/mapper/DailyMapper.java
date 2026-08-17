package com.blog.mapper;

import com.blog.pojo.dto.DailyPageQueryDTO;
import com.blog.pojo.entity.Daily;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 日常 Mapper
 */
@Mapper
public interface DailyMapper {

    /**
     * 分页查询逻辑删除的日常（回收站）
     * @param params 查询参数
     * @return 日常列表
     */
    List<Daily> recyclePageQuery(DailyPageQueryDTO params);

    /**
     * 批量恢复（逻辑删除 -> 正常）
     * @param ids 日常ID集合
     */
    void recoverBatch(@Param("ids") List<Long> ids);

    /**
     * 批量逻辑删除
     * @param ids 日常ID集合
     */
    void logicDelete(@Param("ids") List<Long> ids);

    /**
     * 批量彻底删除
     * @param ids 日常ID集合
     */
    void deleteBatch(@Param("ids") List<Long> ids);
}
