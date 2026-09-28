package com.blog.service;

import com.blog.pojo.vo.InitNoticeVO;
import com.blog.result.PageResult;

public interface NoticeService {

    void createNotice(String type, String title, String actionText, String targetType,
                      String targetTitle, Long targetId, String operatorName, String content);

    InitNoticeVO getInitUnread();

    PageResult pageNotice(Integer page, Integer pageSize);

    boolean markReadSingle(Long id);

    boolean markReadAll();

    Long getOnlineCount();
}
