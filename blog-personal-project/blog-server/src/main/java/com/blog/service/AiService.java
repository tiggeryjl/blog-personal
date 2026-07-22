package com.blog.service;

import java.util.List;

public interface AiService {

    /**
     * 通用对话接口
     * @param userMessage
     * @return
     */
    String chat(String userMessage);

    /**
     * 使用Pro对话
     * @param userMessage
     * @return
     */
    String chatWithPro(String userMessage);

    /**
     * 带System Prompt的对话
     * @param systemPrompt
     * @param userMessage
     * @return
     */
    String chatWithSystem(String systemPrompt, String userMessage);

    /**
     * 根据文章内容自动生成摘要,可自定义最大长度
     * @param content
     * @param maxLength
     * @return
     */
    String generateSummary(String content, int maxLength);

    /**
     * 根据文章内容自动生成摘要,默认 150 字摘要
     * @param content
     * @return
     */
    String generateSummary(String content);

    /**
     * 文章润色/改写（提升表达或调整风格）
     * @param content 原文
     * @param style 风格要求（如“更正式”、“更生动”等）
     * @return 润色后的文本
     */
    String polishArticle(String content, String style);

    /**
     * 翻译（中英互译）
     * @param text 待翻译文本
     * @param targetLang 目标语言（如“英文”、“中文”）
     * @return 翻译结果
     */
    String translate(String text, String targetLang);

    /**
     * 写文章
     * @param topic
     * @param style
     * @param length
     * @return
     */
    String writeArticle(String topic, String style, String length);

    /**
     * 生成标题
     * @param content
     * @return
     */
    List<String> generateTitles(String content);

    /**
     * 日志分析
     * @param logContent
     * @return
     */
    String analyzeLogs(String logContent);

    /**
     * SEO建议-用Flash
     * @param content
     * @return
     */
    String getSeoAdvice(String content);
}
