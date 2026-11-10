package com.blog.utils;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArticleUtil {

    private static final Pattern WORD_PATTERN = Pattern.compile(
            "\\p{IsHan}|[A-Za-z0-9]+(?:['’-][A-Za-z0-9]+)*"
    );

    /**
     * 生成文章摘要（自动截取纯文本前 150 字）
     */
    public static String generateSummary(String content, int maxLength) {
        if (content == null || content.isEmpty()) {
            return "暂无摘要";
        }
        // 1. 过滤 HTML 标签，只保留纯文本
        String plainText = Jsoup.clean(content, Safelist.none());
        // 2. 去掉多余空白和换行
        plainText = plainText.replaceAll("\\s+", " ").trim();
        // 3. 截取前 N 个字符
        if (plainText.length() <= maxLength) {
            return plainText;
        }
        return plainText.substring(0, maxLength) + "...";
    }

    /**
     * 每个汉字计 1，连续英文单词或数字计 1，忽略 HTML、空白和标点。
     */
    public static long countWords(String content) {
        if (content == null || content.isBlank()) {
            return 0L;
        }
        String plainText = Jsoup.parse(content).text();
        Matcher matcher = WORD_PATTERN.matcher(plainText);
        long count = 0L;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

}
