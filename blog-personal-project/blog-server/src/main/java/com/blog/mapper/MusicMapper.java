package com.blog.mapper;

import com.blog.pojo.dto.MusicPageQueryDTO;
import com.blog.pojo.entity.Music;
import com.blog.pojo.vo.MusicVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 音乐 Mapper
 */
@Mapper
public interface MusicMapper {

    /**
     * 分页查询音乐
     *
     * @param dto 查询参数
     * @return 音乐列表
     */
    List<MusicVO> pageQuery(MusicPageQueryDTO dto);

    /**
     * 查询上架音乐（用户端播放器）
     *
     * @return 音乐列表
     */
    List<MusicVO> listPublic();

    /**
     * 根据ID查询音乐
     *
     * @param id 音乐ID
     * @return 音乐信息
     */
    MusicVO getById(@Param("id") Long id);

    /**
     * 新增音乐
     *
     * @param music 音乐实体
     * @return 影响行数
     */
    int add(Music music);

    /**
     * 更新音乐
     *
     * @param music 音乐实体
     * @return 影响行数
     */
    int update(Music music);

    /**
     * 修改上下架状态
     *
     * @param id     音乐ID
     * @param status 1上架 0下架
     * @return 影响行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 批量逻辑删除
     *
     * @param ids 音乐ID集合
     * @return 影响行数
     */
    int logicDelete(@Param("ids") List<Long> ids);
}
