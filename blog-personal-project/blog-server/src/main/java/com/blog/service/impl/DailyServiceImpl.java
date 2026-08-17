package com.blog.service.impl;

import com.blog.exception.CustomException;
import com.blog.mapper.DailyMapper;
import com.blog.pojo.dto.DailyPageQueryDTO;
import com.blog.pojo.entity.Daily;
import com.blog.result.PageResult;
import com.blog.service.DailyService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 日常 Service 实现
 */
@Slf4j
@Service
public class DailyServiceImpl implements DailyService {

    @Autowired
    private DailyMapper dailyMapper;

    /**
     * 分页查询逻辑删除的日常（回收站）
     */
    @Override
    public PageResult recyclePageQuery(DailyPageQueryDTO dailyPageQueryDTO) {
        PageHelper.startPage(dailyPageQueryDTO.getPage(), dailyPageQueryDTO.getPageSize());
        List<Daily> dailyList = dailyMapper.recyclePageQuery(dailyPageQueryDTO);
        PageInfo<Daily> pageInfo = new PageInfo<>(dailyList);
        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    /**
     * 批量恢复（回收站 -> 正常列表）
     */
    @Override
    public void recover(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new CustomException("请选择要恢复的日常");
        }
        dailyMapper.recoverBatch(ids);
    }

    /**
     * 批量逻辑删除
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logicDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new CustomException("请选择要删除的日常");
        }
        dailyMapper.logicDelete(ids);
    }

    /**
     * 批量彻底删除
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new CustomException("请选择要删除的日常");
        }
        dailyMapper.deleteBatch(ids);
    }
}
