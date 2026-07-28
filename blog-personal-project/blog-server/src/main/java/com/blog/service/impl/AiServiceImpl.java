package com.blog.service.impl;

import com.blog.service.AiService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class AiServiceImpl implements AiService {


    @Qualifier("flashChatClient")
    private final ChatClient flashClient;

    @Qualifier("proChatClient")
    private final ChatClient proClient;

    /**
     * 路由决策
     * @param userMessage 用户信息
     * @param taskType 所需模型类型
     * @return
     */
    private ChatClient decideClient(String userMessage, String taskType) {
        return shouldUsePro(userMessage, taskType) ? proClient : flashClient;
    }
    private boolean shouldUsePro(String input, String taskType) {
        // 1. 任务类型硬规则
        if ("writing".equals(taskType) || "log-analysis".equals(taskType)) {
            return true; // 写文章、日志分析 → Pro
        }

        // 2. 超长内容（>1500字符）
        if (input.length() > 1500) {
            return true;
        }

        // 3. 含完整代码块
        if (input.contains("```") && input.length() > 500) {
            return true;
        }

        // 4. 深度关键词触发
        List<String> proKeywords = Arrays.asList(
                "分析", "深度", "详细", "全面", "深入","策略", "对比",
                "梳理", "拆解", "设计","架构", "原理", "源码", "实现");
        for (String kw : proKeywords) {
            if (input.contains(kw)) {
                return true;
            }
        }

        // 5. 明确要求高质量
        if (input.contains("高质量") || input.contains("专业")) {
            return true;
        }

        return false;
    }

    /**
     * 流式对话 - 逐字返回
     * @param userMessage 用户输入
     * @return Flux<String> 流式数据流
     */
    public Flux<String> chatStream(String userMessage) {
        // 决定用 Flash 还是 Pro
        ChatClient baseClient = decideClient(userMessage, "chat");
        log.info("AI流式对话：{}",userMessage);
        // 调用 stream()
        return baseClient.prompt()
                .user(userMessage)
                .stream()
                .content();
    }

    /**
     * 通用对话接口
     * @param userMessage
     * @return
     */
    @Override
    public String chat(String userMessage) {
        ChatClient client = decideClient(userMessage, "chat");
        log.info("AI通用对话：{}",userMessage);
        return client.prompt()
                .user(userMessage)
                .call()
                .content();
    }

    /**
     * 使用Pro对话
     */
    public String chatWithPro(String userMessage) {
        return proClient.prompt()
                .user(userMessage)
                .call()
                .content();
    }

    /**
     * 带System Prompt的对话
     */
    public String chatWithSystem(String systemPrompt, String userMessage) {
        ChatClient client = decideClient(userMessage, "chat");
        return client.prompt()
                .system(systemPrompt)
                .user(userMessage)
                .call()
                .content();
    }

    /**
     * 根据文章内容自动生成摘要
     * @param content
     * @param maxLength
     * @return
     */
    @Override
    public String generateSummary(String content, int maxLength) {
        String prompt = String.format(
                "请为以下文章内容生成一个 %d 字以内的摘要，要求简洁、准确，抓住核心观点：\n%s",
                maxLength, content
        );
        return flashClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    /**
     * 重载：默认 150 字摘要
     * @param content
     * @return
     */
    @Override
    @Async
    public String generateSummary(String content) {
        return generateSummary(content, 150);
    }

    /**
     * 文章润色/改写（提升表达或调整风格）
     * @param content 原文
     * @param style 风格要求（如“更正式”、“更生动”等）
     * @return 润色后的文本
     */
    @Override
    @Async
    public String polishArticle(String content, String style) {
        String prompt = String.format(
                "请对以下文章进行润色，使表达更加 %s，保持原意不变：\n%s",
                style, content
        );
        ChatClient client = decideClient(prompt, "polish");
        return client.prompt()
                .user(prompt)
                .call()
                .content();
    }

    /**
     * 翻译（中英互译）
     * @param text 待翻译文本
     * @param targetLang 目标语言（如“英文”、“中文”）
     * @return 翻译结果
     */
    @Override
    @Async
    public String translate(String text, String targetLang) {
        String prompt = String.format(
                "请将以下内容翻译成 %s：\n%s",
                targetLang, text
        );
        return flashClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    /**
     * 写文章-用pro
     * @param topic
     * @param style
     * @param length
     * @return
     */
    public String writeArticle(String topic, String style, String length) {
        String prompt = String.format(
                "你是一名资深技术博主。请写一篇关于「%s」的技术文章，风格要求：%s，篇幅：%s。\n" +
                        "要求：结构清晰（包含引言、正文、总结）、有代码示例、语言通俗易懂。",
                topic, style, length
        );
        return proClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    /**
     * 生成标题-用Flash
     * @param content
     * @return
     */
    public List<String> generateTitles(String content) {
        String prompt = String.format(
                "请为以下文章生成10个吸引眼球的标题，要求覆盖不同风格（悬念型、干货型、数字型、提问型），每个标题用「||」分隔：\n%s",
                content
        );
        String result = flashClient.prompt()
                .user(prompt)
                .call()
                .content();
        return Arrays.stream(result.split("\\|\\|"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 日志分析-用pro
     * @param logContent
     * @return
     */
    public String analyzeLogs(String logContent) {
        String truncated = logContent.length() > 3000
                ? logContent.substring(0, 3000) + "...\n[日志已截断，仅显示前3000字符]"
                : logContent;
        String prompt = String.format(
                "请分析以下错误日志，归类错误类型，并给出修复建议（按优先级排序）：\n%s",
                truncated
        );
        return proClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    /**
     * SEO建议-用Flash
     * @param content
     * @return
     */
    public String getSeoAdvice(String content) {
        String prompt = String.format(
                "请为以下文章提供SEO优化建议，包括：\n" +
                        "1. 目标关键词（3-5个）\n" +
                        "2. Meta描述优化建议（150字内）\n" +
                        "3. 内链建议（建议关联哪些主题）\n" +
                        "4. 标题优化建议\n\n" +
                        "文章内容：\n%s",
                content
        );
        return flashClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}

