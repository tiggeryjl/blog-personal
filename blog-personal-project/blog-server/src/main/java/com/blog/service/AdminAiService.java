package com.blog.service;

import com.blog.pojo.dto.WeeklyStatsDTO;

public interface AdminAiService {

    /**
     * 生成周报（自动拉取数据 + AI分析）
     */
    String generateWeeklyReport(WeeklyStatsDTO stats);

    /**
     * 内容方向建议
     */
    String suggestTopics(java.util.List<String> existingTags);

    /**
     * 智能评论回复建议
     */
    String suggestReply(String commentContent, String articleTitle);
}
