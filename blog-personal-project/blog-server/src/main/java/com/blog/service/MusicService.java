package com.blog.service;

import com.blog.pojo.dto.MusicDTO;
import com.blog.pojo.dto.MusicPageQueryDTO;
import com.blog.pojo.vo.MusicVO;
import com.blog.result.PageResult;

import java.util.List;

/**
 * 音乐服务
 */
public interface MusicService {

    /**
     * 分页查询音乐
     *
     * @param dto 查询参数
     * @return 分页结果
     */
    PageResult pageQuery(MusicPageQueryDTO dto);

    /**
     * 查询上架音乐（用户端播放器）
     *
     * @return 音乐列表
     */
    List<MusicVO> listPublic();

    /**
     * 根据ID查询
     *
     * @param id 音乐ID
     * @return 音乐信息
     */
    MusicVO getById(Long id);

    /**
     * 新增音乐
     *
     * @param dto 音乐参数
     */
    void add(MusicDTO dto);

    /**
     * 编辑音乐
     *
     * @param dto 音乐参数
     */
    void update(MusicDTO dto);

    /**
     * 修改上下架状态
     *
     * @param id     音乐ID
     * @param status 1上架 0下架
     */
    void updateStatus(Long id, Integer status);

    /**
     * 批量删除
     *
     * @param ids 音乐ID集合
     */
    void delete(List<Long> ids);
}
