package com.blog.service.impl;

import com.blog.WebSocket.AdminNoticeWebSocket;
import com.blog.constant.StatusConstant;
import com.blog.mapper.SysNoticeMapper;
import com.blog.pojo.dto.SysNoticeDTO;
import com.blog.pojo.entity.SysNotice;
import com.blog.pojo.vo.InitNoticeVO;
import com.blog.result.PageResult;
import com.blog.service.NoticeService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NoticeServiceImpl implements NoticeService {

    @Autowired
    private SysNoticeMapper sysNoticeMapper;
    @Autowired
    private AdminNoticeWebSocket adminNoticeWebSocket;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final int TARGET_TITLE_MAX_LENGTH = 255;

    @Override
    @Transactional
    public void createNotice(String type, String title, String actionText, String targetType,
                             String targetTitle, Long targetId, String operatorName, String content) {
        SysNotice notice = new SysNotice();
        notice.setType(type);
        notice.setTitle(title);
        notice.setActionText(actionText);
        notice.setTargetType(targetType);
        notice.setTargetTitle(normalizeTargetTitle(targetTitle, targetType, targetId));
        notice.setTargetId(targetId);
        notice.setOperatorName(operatorName);
        notice.setContent(content);
        notice.setIsRead(StatusConstant.DISABLE);
        notice.setCreateTime(LocalDateTime.now());
        sysNoticeMapper.insert(notice);

        broadcastAfterCommit(convert(notice));
    }

    public InitNoticeVO getInitUnread() {
        long total = sysNoticeMapper.selectUnreadCount();
        List<SysNotice> list = sysNoticeMapper.selectLatest5Unread();
        List<SysNoticeDTO> noticeVOList = list.stream().map(this::convert).collect(Collectors.toList());
        InitNoticeVO vo = new InitNoticeVO();
        vo.setUnreadTotal(total);
        vo.setLatestList(noticeVOList);
        return vo;
    }

    public PageResult pageNotice(Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        List<SysNotice> noticeList = sysNoticeMapper.pageQuery(page, pageSize);
        PageInfo<SysNotice> pageInfo = new PageInfo<>(noticeList);
        List<SysNoticeDTO> rows = pageInfo.getList().stream().map(this::convert).collect(Collectors.toList());
        return new PageResult(pageInfo.getTotal(), rows);
    }

    public boolean markReadSingle(Long id) {
        return sysNoticeMapper.updateReadById(id) > 0;
    }

    public boolean markReadAll() {
        return sysNoticeMapper.updateAllRead() > 0;
    }

    @Override
    public Long getOnlineCount() {
        //在线数量由WebSocket会话统计，同一管理员多端登录分别计数
        return AdminNoticeWebSocket.getOnlineCount();
    }

    private SysNoticeDTO convert(SysNotice e) {
        SysNoticeDTO m = new SysNoticeDTO();
        m.setId(e.getId());
        m.setType(e.getType());
        m.setTitle(e.getTitle());
        m.setActionText(e.getActionText());
        m.setTargetType(e.getTargetType());
        m.setTargetTitle(e.getTargetTitle());
        m.setTargetId(e.getTargetId());
        m.setOperatorName(e.getOperatorName());
        m.setContent(e.getContent());
        m.setIsRead(e.getIsRead());
        m.setCreateTime(e.getCreateTime() == null ? null : e.getCreateTime().format(FORMATTER));
        return m;
    }

    private void broadcastAfterCommit(SysNoticeDTO notice) {
        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    broadcastSafely(notice);
                }
            });
            return;
        }
        broadcastSafely(notice);
    }

    private void broadcastSafely(SysNoticeDTO notice) {
        try {
            adminNoticeWebSocket.broadcast(notice);
        } catch (RuntimeException e) {
            log.error("后台通知推送失败，通知记录已保留，noticeId={}", notice.getId(), e);
        }
    }

    private String normalizeTargetTitle(String targetTitle, String targetType, Long targetId) {
        String normalized = targetTitle == null ? "" : targetTitle.trim().replaceAll("\\s+", " ");
        if (normalized.isEmpty()) {
            normalized = (targetType == null ? "目标" : targetType) + " #" + targetId;
        }
        if (normalized.codePointCount(0, normalized.length()) <= TARGET_TITLE_MAX_LENGTH) {
            return normalized;
        }
        int endIndex = normalized.offsetByCodePoints(0, TARGET_TITLE_MAX_LENGTH);
        return normalized.substring(0, endIndex);
    }
}
