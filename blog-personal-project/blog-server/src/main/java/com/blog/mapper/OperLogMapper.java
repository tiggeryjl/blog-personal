package com.blog.mapper;

import com.blog.pojo.dto.OperLogPageQueryDTO;
import com.blog.pojo.entity.SysOperLog;
import com.blog.pojo.vo.OperLogVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 后台操作日志 Mapper
 */
@Mapper
public interface OperLogMapper {

    /**
     * 写入一条操作日志
     *
     * @param operLog 日志实体
     * @return 影响行数
     */
    int insert(SysOperLog operLog);

    /**
     * 分页查询操作日志（不返回入参、结果等大字段）
     *
     * @param dto 查询参数
     * @return 日志列表
     */
    List<OperLogVO> pageQuery(OperLogPageQueryDTO dto);

    /**
     * 根据ID查询日志详情
     *
     * @param id 日志ID
     * @return 日志详情
     */
    OperLogVO getById(@Param("id") Long id);

    /**
     * 按ID批量删除
     *
     * @param ids 日志ID集合
     * @return 影响行数
     */
    int deleteByIds(@Param("ids") List<Long> ids);

    /**
     * 清空全部日志
     *
     * @return 影响行数
     */
    int clearAll();

    /**
     * 删除指定时间之前的日志
     *
     * @param time 时间点
     * @return 影响行数
     */
    int deleteBefore(@Param("time") LocalDateTime time);
}
