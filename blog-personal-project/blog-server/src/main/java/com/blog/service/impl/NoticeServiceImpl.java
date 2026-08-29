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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NoticeServiceImpl implements NoticeService {

    @Autowired
    private SysNoticeMapper sysNoticeMapper;
    @Autowired
    private AdminNoticeWebSocket adminNoticeWebSocket;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy‑MM‑dd HH:mm:ss");

    @Override
    @Transactional
    public void createNotice(String type, String title, String actionText, String articleTitle, Long articleId, String operatorName, String content) {
        SysNotice notice = new SysNotice();
        notice.setType(type);
        notice.setTitle(title);
        notice.setActionText(actionText);
        notice.setArticleTitle(articleTitle);
        notice.setArticleId(articleId);
        notice.setOperatorName(operatorName);
        notice.setContent(content);
        notice.setIsRead(StatusConstant.DISABLE);
        notice.setCreateTime(LocalDateTime.now());
        sysNoticeMapper.insert(notice);

        SysNoticeDTO  sysNoticeDTO = new SysNoticeDTO();
        BeanUtils.copyProperties(notice, sysNoticeDTO);
        sysNoticeDTO.setCreateTime(notice.getCreateTime().format(FORMATTER));

        adminNoticeWebSocket.broadcast(sysNoticeDTO);
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
        List<SysNoticeDTO> noticeList = sysNoticeMapper.pageQuery(page, pageSize);
        PageInfo<SysNoticeDTO> pageInfo = new PageInfo<>(noticeList);
        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    public boolean markReadSingle(Long id) {
        return sysNoticeMapper.updateReadById(id) > 0;
    }

    public boolean markReadAll() {
        return sysNoticeMapper.updateAllRead() > 0;
    }

    private SysNoticeDTO convert(SysNotice e){
        SysNoticeDTO m = new SysNoticeDTO();
        m.setId(e.getId());
        m.setType(e.getType());
        m.setTitle(e.getTitle());
        m.setActionText(e.getActionText());
        m.setArticleTitle(e.getArticleTitle());
        m.setArticleId(e.getArticleId());
        m.setOperatorName(e.getOperatorName());
        m.setContent(e.getContent());
        m.setIsRead(e.getIsRead());
        m.setCreateTime(e.getCreateTime().format(FORMATTER));
        return m;
    }
}
