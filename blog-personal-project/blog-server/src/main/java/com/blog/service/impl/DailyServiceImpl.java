package com.blog.service.impl;

import com.blog.constant.DailyStatusConstant;
import com.blog.constant.DelStatusConstant;
import com.blog.constant.StatusConstant;
import com.blog.context.BaseContext;
import com.blog.exception.CustomException;
import com.blog.mapper.DailyMapper;
import com.blog.mapper.SysUserMapper;
import com.blog.pojo.dto.DailyDTO;
import com.blog.pojo.dto.DailyPageQueryDTO;
import com.blog.pojo.entity.Daily;
import com.blog.pojo.entity.SysUser;
import com.blog.result.PageResult;
import com.blog.service.DailyService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 日常 Service 实现
 */
@Slf4j
@Service
public class DailyServiceImpl implements DailyService {

    @Autowired
    private DailyMapper dailyMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    /**
     * 分页查询正常列表的日常
     */
    @Override
    public PageResult pageQuery(DailyPageQueryDTO dailyPageQueryDTO) {
        PageHelper.startPage(dailyPageQueryDTO.getPage(), dailyPageQueryDTO.getPageSize());
        List<Daily> dailyList = dailyMapper.pageQuery(dailyPageQueryDTO);
        PageInfo<Daily> pageInfo = new PageInfo<>(dailyList);
        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    /**
     * 新增日常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(DailyDTO dailyDTO) {
        if (dailyDTO.getContent() == null || dailyDTO.getContent().trim().isEmpty()) {
            throw new CustomException("日常内容不能为空");
        }

        Daily daily = new Daily();
        BeanUtils.copyProperties(dailyDTO, daily);
        daily.setContent(dailyDTO.getContent().trim());
        daily.setImages(joinUrls(dailyDTO.getImages()));
        daily.setFiles(joinUrls(dailyDTO.getFiles()));
        daily.setType(resolveType(dailyDTO.getType(), daily.getImages(), daily.getFiles()));
        daily.setIsTop(dailyDTO.getIsTop() == null ? StatusConstant.DISABLE : dailyDTO.getIsTop());
        daily.setStatus(dailyDTO.getStatus() == null ? DailyStatusConstant.DRAFT : dailyDTO.getStatus());
        daily.setSort(dailyDTO.getSort() == null ? 0 : dailyDTO.getSort());
        daily.setLikeNum(0);
        daily.setCommentNum(0);
        daily.setCreateTime(LocalDateTime.now());
        daily.setUpdateTime(LocalDateTime.now());
        if (DailyStatusConstant.PUBLISHED.equals(daily.getStatus())) {
            daily.setPublishTime(LocalDateTime.now());
        }
        daily.setDeleteFlag(DelStatusConstant.ENABLE);

        SysUser user = sysUserMapper.getByUserId(BaseContext.getCurrentId());
        if (user != null) {
            daily.setUserId(user.getId());
            daily.setUserNickname(user.getNickname());
            daily.setUserAvatar(user.getAvatar());
        } else {
            daily.setUserId(BaseContext.getCurrentId());
        }

        dailyMapper.add(daily);
    }

    /**
     * 根据ID查询日常
     */
    @Override
    public Daily getById(Long id) {
        Daily daily = dailyMapper.getById(id);
        if (daily == null) {
            throw new CustomException("日常不存在或已被删除");
        }
        return daily;
    }

    /**
     * 修改日常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(DailyDTO dailyDTO) {
        if (dailyDTO.getId() == null) {
            throw new CustomException("日常ID不能为空");
        }
        Daily oldDaily = dailyMapper.getById(dailyDTO.getId());
        if (oldDaily == null) {
            throw new CustomException("日常不存在或已被删除");
        }
        if (dailyDTO.getContent() == null || dailyDTO.getContent().trim().isEmpty()) {
            throw new CustomException("日常内容不能为空");
        }

        Daily daily = new Daily();
        daily.setId(dailyDTO.getId());
        daily.setContent(dailyDTO.getContent().trim());
        daily.setImages(joinUrls(dailyDTO.getImages()));
        daily.setFiles(joinUrls(dailyDTO.getFiles()));
        daily.setType(resolveType(dailyDTO.getType(), daily.getImages(), daily.getFiles()));
        daily.setSort(dailyDTO.getSort());
        daily.setUpdateTime(LocalDateTime.now());

        dailyMapper.update(daily);
    }

    /**
     * 修改日常状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(DailyDTO dailyDTO) {
        if (dailyDTO.getId() == null || dailyDTO.getStatus() == null) {
            throw new CustomException("日常ID、目标状态不能为空");
        }

        Daily oldDaily = dailyMapper.getById(dailyDTO.getId());
        if (oldDaily == null) {
            throw new CustomException("日常不存在或已被删除");
        }

        Daily daily = new Daily();
        daily.setId(dailyDTO.getId());
        daily.setStatus(dailyDTO.getStatus());
        daily.setUpdateTime(LocalDateTime.now());

        // 立即发布：草稿、已下架、定时、私密均可转为已发布
        if (DailyStatusConstant.PUBLISHED.equals(dailyDTO.getStatus()) && List.of(
                DailyStatusConstant.DRAFT,
                DailyStatusConstant.REMOVED,
                DailyStatusConstant.SCHEDULED,
                DailyStatusConstant.PRIVATE).contains(oldDaily.getStatus())) {
            daily.setPublishTime(LocalDateTime.now());
        }
        // 下架：仅已发布可下架
        else if (DailyStatusConstant.REMOVED.equals(dailyDTO.getStatus())
                && DailyStatusConstant.PUBLISHED.equals(oldDaily.getStatus())) {
            // 仅修改状态
        }
        // 设为私密：已发布、定时可转为私密
        else if (DailyStatusConstant.PRIVATE.equals(dailyDTO.getStatus())
                && (DailyStatusConstant.PUBLISHED.equals(oldDaily.getStatus())
                || DailyStatusConstant.SCHEDULED.equals(oldDaily.getStatus()))) {
            // 仅修改状态
        } else {
            throw new CustomException("当前状态不允许变更为目标状态");
        }

        dailyMapper.updateStatus(daily);
    }

    /**
     * 切换置顶状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTop(Long id) {
        Daily oldDaily = dailyMapper.getById(id);
        if (oldDaily == null) {
            throw new CustomException("日常不存在或已被删除");
        }
        if (!DailyStatusConstant.PUBLISHED.equals(oldDaily.getStatus())) {
            throw new CustomException("当前日常状态不允许执行置顶操作");
        }

        Integer newIsTop = StatusConstant.ENABLE.equals(oldDaily.getIsTop())
                ? StatusConstant.DISABLE : StatusConstant.ENABLE;
        Daily daily = Daily.builder()
                .id(id)
                .isTop(newIsTop)
                .updateTime(LocalDateTime.now())
                .build();
        dailyMapper.updateTop(daily);
    }

    /**
     * 设置定时发布
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setTimedPublish(DailyDTO dailyDTO) {
        if (dailyDTO.getId() == null) {
            throw new CustomException("日常ID不能为空");
        }
        Daily oldDaily = dailyMapper.getById(dailyDTO.getId());
        if (oldDaily == null) {
            throw new CustomException("日常不存在或已被删除");
        }

        // 仅草稿、已下架状态可设置定时发布
        if (!List.of(DailyStatusConstant.DRAFT, DailyStatusConstant.REMOVED).contains(oldDaily.getStatus())) {
            throw new CustomException("仅草稿和已下架状态的日常可设置定时发布");
        }
        if (dailyDTO.getTimedPublishTime() == null) {
            throw new CustomException("请选择定时发布时间");
        }
        if (dailyDTO.getTimedPublishTime().isBefore(LocalDateTime.now())) {
            throw new CustomException("定时发布时间必须晚于当前时间");
        }

        Daily daily = Daily.builder()
                .id(dailyDTO.getId())
                .timedPublishTime(dailyDTO.getTimedPublishTime())
                .status(DailyStatusConstant.SCHEDULED)
                .updateTime(LocalDateTime.now())
                .build();
        dailyMapper.updateTimedPublish(daily);
    }

    /**
     * 取消定时发布
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelTimedPublish(Long id) {
        Daily oldDaily = dailyMapper.getById(id);
        if (oldDaily == null) {
            throw new CustomException("日常不存在或已被删除");
        }
        if (!DailyStatusConstant.SCHEDULED.equals(oldDaily.getStatus())) {
            throw new CustomException("当前日常不是定时发布状态，无法取消");
        }

        Daily daily = Daily.builder()
                .id(id)
                .status(DailyStatusConstant.DRAFT)
                .updateTime(LocalDateTime.now())
                .build();
        dailyMapper.cancelTimedPublish(daily);
    }

    /**
     * 图片/文件列表转逗号分隔字符串
     */
    private String joinUrls(List<String> urls) {
        if (urls == null) {
            return null;
        }
        return urls.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .collect(Collectors.joining(","));
    }

    /**
     * 计算日常类型：0纯文字 1图片 2文件 3图文混合
     */
    private Integer resolveType(Integer type, String images, String files) {
        if (type != null) {
            return type;
        }
        boolean hasImages = StringUtils.hasText(images);
        boolean hasFiles = StringUtils.hasText(files);
        if (hasImages && hasFiles) {
            return 3;
        }
        if (hasImages) {
            return 1;
        }
        if (hasFiles) {
            return 2;
        }
        return 0;
    }

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
