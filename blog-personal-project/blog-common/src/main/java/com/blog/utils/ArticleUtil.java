package com.blog.utils;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

public class ArticleUtil {

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

}
