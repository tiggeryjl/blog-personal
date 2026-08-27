package com.blog.mapper;

import com.blog.pojo.dto.DailyPageQueryDTO;
import com.blog.pojo.entity.Daily;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 日常 Mapper
 */
@Mapper
public interface DailyMapper {

    /**
     * 分页查询正常列表的日常
     * @param params 查询参数
     * @return 日常列表
     */
    List<Daily> pageQuery(DailyPageQueryDTO params);

    /**
     * 分页查询逻辑删除的日常（回收站）
     * @param params 查询参数
     * @return 日常列表
     */
    List<Daily> recyclePageQuery(DailyPageQueryDTO params);

    /**
     * 根据ID查询日常（正常列表，不含回收站数据）
     * @param id 日常ID
     * @return 日常信息
     */
    Daily getById(Long id);

    /**
     * 浏览数 +1
     * @param id 日常ID
     */
    void incrementView(Long id);

    /**
     * 新增日常
     * @param daily 日常信息
     */
    void add(Daily daily);

    /**
     * 修改日常内容
     * @param daily 日常信息
     */
    void update(Daily daily);

    /**
     * 修改日常状态（含发布/下架/私密等，自动处理发布时间与定时时间）
     * @param daily 日常信息
     */
    void updateStatus(Daily daily);

    /**
     * 切换置顶状态
     * @param daily 日常信息
     */
    void updateTop(Daily daily);

    /**
     * 设置定时发布
     * @param daily 日常信息
     */
    void updateTimedPublish(Daily daily);

    /**
     * 取消定时发布
     * @param daily 日常信息
     */
    void cancelTimedPublish(Daily daily);

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

    /**
     * 批量发布到期的定时日常
     * @param now 当前时间
     * @return 发布条数
     */
    int batchPublishExpiredTimed(LocalDateTime now);
}
