package com.blog.utils;

/**
 * 通知展示文本处理工具
 */
public final class NoticeTextUtil {

    private static final int DAILY_SUMMARY_LENGTH = 50;

    public static String dailySummary(String content, Long dailyId) {
        String normalized = content == null ? "" : content.trim().replaceAll("\\s+", " ");
        if (normalized.isEmpty()) {
            return "日常 #" + dailyId;
        }
        if (normalized.codePointCount(0, normalized.length()) <= DAILY_SUMMARY_LENGTH) {
            return normalized;
        }
        int endIndex = normalized.offsetByCodePoints(0, DAILY_SUMMARY_LENGTH);
        return normalized.substring(0, endIndex) + "...";
    }
}
