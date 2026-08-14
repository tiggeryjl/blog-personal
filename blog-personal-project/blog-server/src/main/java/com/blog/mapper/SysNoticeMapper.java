package com.blog.mapper;

import com.blog.pojo.dto.SysNoticeDTO;
import com.blog.pojo.entity.SysNotice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SysNoticeMapper {

    /**
     * 分页查询通知信息
     * @param page
     * @param pageSize
     * @return
     */
    @Select("SELECT *  FROM sys_notice ORDER BY create_time DESC")
    List<SysNoticeDTO> pageQuery(Integer page, Integer pageSize);

    /**
     * 统计未读个数
     * @return
     */
    @Select("SELECT COUNT(id) FROM sys_notice WHERE is_read = 0")
    long selectUnreadCount();

    /**
     * 查询最新5条未读信息
     * @return
     */
    @Select("SELECT * FROM sys_notice WHERE is_read = 0 ORDER BY create_time DESC LIMIT 5")
    List<SysNotice> selectLatest5Unread();

    /**
     * 标记单条已读
     * @param id
     * @return
     */
    @Update("UPDATE sys_notice SET is_read=1 WHERE id=#{id}")
    int updateReadById(@Param("id") Long id);

    /**
     * 一键全部标记已读
     * @return
     */
    @Update("UPDATE sys_notice SET is_read=1 WHERE is_read=0")
    int updateAllRead();

    void insert(SysNotice notice);

}
