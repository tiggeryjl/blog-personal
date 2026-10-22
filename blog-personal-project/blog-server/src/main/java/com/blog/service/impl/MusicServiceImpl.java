package com.blog.service.impl;

import com.blog.constant.DelStatusConstant;
import com.blog.constant.StatusConstant;
import com.blog.exception.OperationNotAllowedException;
import com.blog.mapper.MusicMapper;
import com.blog.pojo.dto.MusicDTO;
import com.blog.pojo.dto.MusicPageQueryDTO;
import com.blog.pojo.entity.Music;
import com.blog.pojo.vo.MusicVO;
import com.blog.result.PageResult;
import com.blog.service.MusicService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 音乐服务实现
 */
@Service
public class MusicServiceImpl implements MusicService {

    @Autowired
    private MusicMapper musicMapper;

    @Override
    public PageResult pageQuery(MusicPageQueryDTO dto) {
        int page = dto.getPage() == null || dto.getPage() < 1 ? 1 : dto.getPage();
        int pageSize = dto.getPageSize() == null || dto.getPageSize() < 1 ? 10 : dto.getPageSize();
        if (pageSize > 100) {
            pageSize = 100;
        }
        PageHelper.startPage(page, pageSize);
        List<MusicVO> list = musicMapper.pageQuery(dto);
        PageInfo<MusicVO> pageInfo = new PageInfo<>(list);
        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public List<MusicVO> listPublic() {
        return musicMapper.listPublic();
    }

    @Override
    public MusicVO getById(Long id) {
        return musicMapper.getById(id);
    }

    @Override
    public void add(MusicDTO dto) {
        validateStatus(dto.getStatus());
        Music music = new Music();
        BeanUtils.copyProperties(dto, music);
        music.setName(dto.getName().trim());
        music.setUrl(dto.getUrl().trim());
        music.setSinger(dto.getSinger() == null ? "" : dto.getSinger().trim());
        music.setCover(dto.getCover() == null ? "" : dto.getCover().trim());
        music.setLyric(dto.getLyric() == null ? "" : dto.getLyric().trim());
        music.setDuration(dto.getDuration() == null ? 0 : Math.max(0, dto.getDuration()));
        music.setSort(dto.getSort() == null ? 0 : Math.max(0, dto.getSort()));
        music.setStatus(dto.getStatus() == null ? StatusConstant.ENABLE : dto.getStatus());
        music.setDeleteFlag(DelStatusConstant.ENABLE);
        music.setCreateTime(LocalDateTime.now());
        music.setUpdateTime(LocalDateTime.now());
        musicMapper.add(music);
    }

    @Override
    public void update(MusicDTO dto) {
        if (dto.getId() == null) {
            throw new OperationNotAllowedException("音乐ID不能为空");
        }
        validateStatus(dto.getStatus());
        MusicVO exist = musicMapper.getById(dto.getId());
        if (exist == null) {
            throw new OperationNotAllowedException("音乐不存在");
        }
        Music music = new Music();
        BeanUtils.copyProperties(dto, music);
        music.setName(dto.getName().trim());
        music.setUrl(dto.getUrl().trim());
        music.setSinger(dto.getSinger() == null ? "" : dto.getSinger().trim());
        music.setCover(dto.getCover() == null ? "" : dto.getCover().trim());
        music.setLyric(dto.getLyric() == null ? "" : dto.getLyric().trim());
        if (dto.getDuration() != null) {
            music.setDuration(Math.max(0, dto.getDuration()));
        }
        if (dto.getSort() != null) {
            music.setSort(Math.max(0, dto.getSort()));
        }
        music.setUpdateTime(LocalDateTime.now());
        musicMapper.update(music);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        if (id == null) {
            throw new OperationNotAllowedException("音乐ID不能为空");
        }
        validateStatus(status);
        if (musicMapper.getById(id) == null) {
            throw new OperationNotAllowedException("音乐不存在");
        }
        musicMapper.updateStatus(id, status);
    }

    @Override
    public void delete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new OperationNotAllowedException("请选择要删除的音乐");
        }
        musicMapper.logicDelete(ids);
    }

    /**
     * 校验音乐上下架状态
     *
     * @param status 上下架状态
     */
    private void validateStatus(Integer status) {
        if (status != null
                && !StatusConstant.ENABLE.equals(status)
                && !StatusConstant.DISABLE.equals(status)) {
            throw new OperationNotAllowedException("状态不合法");
        }
    }
}
