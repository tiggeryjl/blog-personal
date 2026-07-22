package com.blog.controller.ai;

import com.blog.pojo.dto.AiRequestDTO;
import com.blog.pojo.dto.WeeklyStatsDTO;
import com.blog.result.Result;
import com.blog.service.AdminAiService;
import com.blog.service.AiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/AiChat/ai")
public class AIController {

    @Autowired
    private AiService aiService;
    @Autowired
    private AdminAiService adminAiService;

    /**
     * 通用对话接口
     * @param message
     * @return
     */
    @GetMapping("/chat")
    public Result<String> chat(@RequestParam String message) {
        // 调用 AI 并返回结果
        String result = aiService.chat(message);
        return Result.success(result);
    }

    /**
     * 写文章初稿
     */
    @PostMapping("/write")
    public Result writeArticle(@RequestBody AiRequestDTO aiRequestDTO) {
        String result = aiService.writeArticle(aiRequestDTO.getTopic(), aiRequestDTO.getStyle(), aiRequestDTO.getLength());
        return Result.success(result);
    }

    /**
     * 文章润色
     */
    @PostMapping("/polish")
    public Result polish(@RequestBody AiRequestDTO aiRequestDTO) {
        String result = aiService.polishArticle(aiRequestDTO.getContent(), aiRequestDTO.getStyle());
        return Result.success(result);
    }

    /**
     * 生成标题
     */
    @PostMapping("/titles")
    public Result generateTitles(@RequestBody String content) {
        List<String> titles = aiService.generateTitles(content);
        return Result.success(titles);
    }

    // ==================== 数据分析 ====================

    /**
     * 生成周报
     */
    @PostMapping("/weekly-report")
    public Result weeklyReport(@RequestBody WeeklyStatsDTO stats) {
        String report = adminAiService.generateWeeklyReport(stats);
        return Result.success(report);
    }

    /**
     * 内容建议
     */
    @GetMapping("/suggestions")
    public Result contentSuggestions(@RequestParam List<String> tags) {
        String suggestions = adminAiService.suggestTopics(tags);
        return Result.success(suggestions);
    }

    // ==================== 运维辅助 ====================

    /**
     * 日志分析
     */
    @PostMapping("/analyze-logs")
    public Result analyzeLogs(@RequestBody String logContent) {
        String analysis = aiService.analyzeLogs(logContent);
        return Result.success(analysis);
    }

    /**
     * SEO建议
     */
    @PostMapping("/seo")
    public Result seoAdvice(@RequestBody String content) {
        String advice = aiService.getSeoAdvice(content);
        return Result.success(advice);
    }

    // ==================== 评论互动 ====================

    /**
     * 评论回复建议
     */
    @PostMapping("/reply-suggest")
    public Result suggestReply(
            @RequestParam String comment,
            @RequestParam String articleTitle
    ) {
        String reply = adminAiService.suggestReply(comment, articleTitle);
        return Result.success(reply);
    }
}
