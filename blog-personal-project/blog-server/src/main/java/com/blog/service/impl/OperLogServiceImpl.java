package com.blog.service.impl;

import com.blog.constant.SystemConstant;
import com.blog.constant.OperLogConstant;
import com.blog.context.BaseContext;
import com.blog.exception.OperationNotAllowedException;
import com.blog.mapper.OperLogMapper;
import com.blog.mapper.SysUserRoleMapper;
import com.blog.pojo.dto.OperLogPageQueryDTO;
import com.blog.pojo.entity.SysOperLog;
import com.blog.pojo.vo.OperLogVO;
import com.blog.result.PageResult;
import com.blog.service.OperLogService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 后台操作日志服务实现
 */
@Slf4j
@Service
public class OperLogServiceImpl implements OperLogService {

    @Autowired
    private OperLogMapper operLogMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Async
    @Override
    public void save(SysOperLog operLog) {
        try {
            operLogMapper.insert(operLog);
        } catch (Exception e) {
            // 日志写入失败不能影响业务
            log.warn("操作日志写入失败：{}", e.getMessage());
        }
    }

    @Override
    public PageResult pageQuery(OperLogPageQueryDTO dto) {
        int page = dto.getPage() == null || dto.getPage() < 1 ? 1 : dto.getPage();
        int pageSize = dto.getPageSize() == null || dto.getPageSize() < 1 ? 10 : dto.getPageSize();
        if (pageSize > 100) {
            pageSize = 100;
        }
        PageHelper.startPage(page, pageSize);
        List<OperLogVO> list = operLogMapper.pageQuery(dto);
        PageInfo<OperLogVO> pageInfo = new PageInfo<>(list);
        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public OperLogVO getById(Long id) {
        return operLogMapper.getById(id);
    }

    @Override
    public void delete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new OperationNotAllowedException("请选择要删除的日志");
        }
        operLogMapper.deleteByIds(ids);
    }

    @Override
    public void clear() {
        // 清空属于不可赋权的操作，只有超级管理员角色可以执行
        Long currentId = BaseContext.getCurrentId();
        if (currentId == null || !userRoleMapper.hasRole(currentId, SystemConstant.SUPER_ADMIN_ROLE)) {
            throw new OperationNotAllowedException("只有超级管理员可以清空操作日志");
        }
        operLogMapper.clearAll();
    }

    @Override
    public int cleanExpired() {
        return operLogMapper.deleteBefore(LocalDateTime.now().minusDays(OperLogConstant.RETENTION_DAYS));
    }
}
