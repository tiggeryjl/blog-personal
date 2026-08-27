package com.blog.service.impl;

import com.blog.pojo.dto.WeeklyStatsDTO;
import com.blog.service.AdminAiService;
import com.blog.service.AiService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AdminAiServiceImpl implements AdminAiService {

    @Autowired
    private AiService aiService;

    /**
     * 生成周报
     */
    public String generateWeeklyReport(WeeklyStatsDTO stats) {
        String prompt = String.format(
                "你是一名数据分析师。基于以下博客本周数据，生成一份简洁的周报（包含：总览、亮点、问题、建议）：\n" +
                        "📊 数据总览：\n" +
                        "- 本周阅读量：%d\n" +
                        "- 本周新增评论：%d\n" +
                        "- 本周新增文章：%d\n" +
                        "- 本周新增用户：%d\n\n" +
                        "🔥 热门文章TOP5（按本周阅读量）：\n%s\n\n" +
                        "请用轻松友好的语气输出，开头加一句鼓励的话。",
                stats.getViews(),
                stats.getComments(),
                stats.getNewArticles(),
                stats.getNewSubscribers(),
                stats.getTop5Articles()
        );
        return aiService.chatWithPro(prompt);
    }

    /**
     * 内容方向建议
     */
    public String suggestTopics(java.util.List<String> existingTags) {
        String tags = String.join("、", existingTags);
        String prompt = String.format(
                "我的博客现有文章标签有：%s。\n" +
                        "请分析哪些技术领域我已经覆盖了，哪些热门领域我缺失了，给出3-5个建议写作方向。\n" +
                        "要求：每个方向附上简短的理由。",
                tags
        );
        return aiService.chat(prompt); // 自动路由
    }

    /**
     * 智能评论回复建议
     */
    public String suggestReply(String commentContent, String articleTitle) {
        String prompt = String.format(
                "读者在文章《%s》下发表了以下评论：\n\"%s\"\n\n" +
                        "请生成3条不同风格的回复建议（1. 专业详细型 2. 亲切感谢型 3. 简洁友好型），每条用「||」分隔。",
                articleTitle, commentContent
        );
        return aiService.chat(prompt);
    }
}

