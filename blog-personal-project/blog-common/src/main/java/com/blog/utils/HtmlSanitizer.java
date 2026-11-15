package com.blog.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

/**
 * 富文本白名单净化：入库前统一处理，防止存储型 XSS
 */
public final class HtmlSanitizer {

    private HtmlSanitizer() {
    }

    /**
     * 文章/日常正文允许的标签与属性
     */
    private static final Safelist ARTICLE_SAFELIST = Safelist.relaxed()
            // 基础排版保留
            .addTags("figure", "figcaption", "video", "source", "hr", "span")
            // 样式与类名（用于 wangEditor 的排版）
            .addAttributes(":all", "class")
            // 图片、视频、链接
            .addAttributes("img", "src", "alt", "title", "width", "height")
            .addAttributes("video", "src", "poster", "controls")
            .addAttributes("source", "src", "type")
            .addAttributes("a", "href", "target", "rel")
            // 只允许安全协议，javascript: / data:text/html 一律清掉
            .addProtocols("img", "src", "http", "https")
            .addProtocols("video", "src", "http", "https")
            .addProtocols("source", "src", "http", "https")
            .addProtocols("a", "href", "http", "https", "mailto");

    /**
     * 净化文章/日常正文
     * 不保留 style 属性，避免 background:url(javascript:) 之类的漏洞
     */
    public static String cleanRichText(String html) {
        if (html == null || html.isBlank()) {
            return html;
        }
        Document.OutputSettings outputSettings = new Document.OutputSettings().prettyPrint(false);
        return Jsoup.clean(html, "", ARTICLE_SAFELIST, outputSettings);
    }

    /**
     * 纯文本字段（评论 / 留言 / 回复 / 昵称）：去掉所有标签，保留文字本身
     * 不做 HTML 实体转义，避免前端 {{ }} 再转义一次出现 &amp; 这类乱码
     */
    public static String cleanPlainText(String text) {
        if (text == null) {
            return null;
        }
        String withBreaks = text.replaceAll("(?i)<br\\s*/?>", "\n");
        Document doc = Jsoup.parseBodyFragment(withBreaks);
        doc.select("script, style").remove();// 脚本内容连内容一起丢掉
        return doc.body().wholeText().replace('\u00A0', ' ').trim();
    }
}
