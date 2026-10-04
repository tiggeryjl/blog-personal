package com.blog.service.impl;

import com.blog.constant.StatusConstant;
import com.blog.context.BaseContext;
import com.blog.exception.RssException;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.RssSubscriptionMapper;
import com.blog.pojo.dto.RssSubscribeDTO;
import com.blog.pojo.entity.RssSubscription;
import com.blog.pojo.vo.ArticleFrontVO;
import com.blog.properties.RssProperties;
import com.blog.service.MailService;
import com.blog.service.RssService;
import com.blog.service.SystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * RSS 订阅源 + 邮箱订阅
 */
@Slf4j
@Service
public class RssServiceImpl implements RssService {

    //新文章通知的时间水位线，存放在 system_config 表，重启不丢
    private static final String NOTIFY_WATERMARK_KEY = "rss_notify_watermark";

    //一次最多通知多少篇
    private static final int MAX_ARTICLES_PER_NOTIFY = 8;

    //一次最多通知多少位订阅者
    private static final int MAX_SUBSCRIBERS_PER_NOTIFY = 500;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final DateTimeFormatter RFC822_FORMATTER =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

    @Autowired
    private RssSubscriptionMapper rssSubscriptionMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private MailService mailService;

    @Autowired
    private RssProperties rssProperties;

    @Override
    public void subscribe(RssSubscribeDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getEmail())) {
            throw new RssException("请填写邮箱");
        }

        String email = dto.getEmail().trim().toLowerCase();
        if (email.length() > 50) {
            throw new RssException("邮箱长度不能超过50个字符");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new RssException("邮箱格式不正确");
        }

        String nickname = StringUtils.hasText(dto.getNickname())
                ? dto.getNickname().trim()
                : email.substring(0, email.indexOf('@'));
        if (nickname.length() > 30) {
            nickname = nickname.substring(0, 30);
        }

        LocalDateTime now = LocalDateTime.now();
        RssSubscription exist = rssSubscriptionMapper.getByEmail(email);

        // 当前登录用户id
        Long currentUserId = BaseContext.getCurrentId();

        // 首次订阅新增数据并激活
        if (exist == null) {
            try {
                rssSubscriptionMapper.insert(RssSubscription.builder()
                        .visitorId(currentUserId)
                        .nickname(nickname)
                        .email(email)
                        .token(generateToken())
                        .isActive(StatusConstant.ENABLE)
                        .subscribeTime(now)
                        .build());
            } catch (DataIntegrityViolationException e) {
                throw new RssException("订阅失败，请稍后再试");
            }
            log.info("新增RSS邮箱订阅：{}", email);
        }

        if (StatusConstant.ENABLE.equals(exist.getIsActive())) {
            throw new RssException("这个邮箱已经订阅过了，新文章发布时会通知你");
        }

        // 之前退订过：换一个新令牌重新激活
        rssSubscriptionMapper.reactivate(RssSubscription.builder()
                .id(exist.getId())
                .visitorId(currentUserId == null ? exist.getVisitorId() : currentUserId)
                .nickname(nickname)
                .token(generateToken())
                .subscribeTime(now)
                .build());
        log.info("RSS邮箱订阅重新激活：{}", email);
    }

    @Override
    public boolean unsubscribe(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }
        int count = rssSubscriptionMapper.unsubscribeByToken(token.trim());
        if (count > 0) {
            log.info("RSS邮箱订阅已退订，token：{}", token);
            return true;
        }
        return false;
    }

    @Override
    public Long countActive() {
        Long count = rssSubscriptionMapper.countActive();
        return count == null ? 0L : count;
    }

    @Override
    public int notifyNewArticles() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime watermark = readWatermark();

        // 首次运行只建立水位线，不把历史文章全发出去
        if (watermark == null) {
            saveWatermark(now);
            log.info("RSS订阅通知首次初始化时间水位线：{}", now);
            return 0;
        }
        if (!watermark.isBefore(now)) {
            return 0;
        }

        List<ArticleFrontVO> articles = articleMapper.selectPublishedBetween(watermark, now);
        if (articles == null || articles.isEmpty()) {
            saveWatermark(now);
            return 0;
        }
        // 只取最近的若干篇，避免积压时一次发太多
        if (articles.size() > MAX_ARTICLES_PER_NOTIFY) {
            articles = articles.subList(articles.size() - MAX_ARTICLES_PER_NOTIFY, articles.size());
        }

        if (!mailService.isAvailable()) {
            // 邮件没配置好时先不推进水位线，等配置完成后可以补发这批文章
            log.info("检测到-{}-篇新文章，但发件邮箱未配置，暂不发送订阅通知", articles.size());
            return 0;
        }

        List<RssSubscription> subscribers = rssSubscriptionMapper.listActive();
        if (subscribers == null || subscribers.isEmpty()) {
            saveWatermark(now);
            return 0;
        }
        if (subscribers.size() > MAX_SUBSCRIBERS_PER_NOTIFY) {
            subscribers = subscribers.subList(0, MAX_SUBSCRIBERS_PER_NOTIFY);
        }

        int sent = 0;
        for (RssSubscription subscriber : subscribers) {
            try {
                mailService.sendHtml(subscriber.getEmail(),
                        buildNoticeSubject(articles),
                        buildNoticeHtml(subscriber, articles));
                sent++;
            } catch (Exception e) {
                // 单个邮箱失败不影响其他人
                log.error("给订阅者-{}-发送通知失败：{}", subscriber.getEmail(), e.getMessage());
            }
        }

        // 全部发送失败，此时不推进水位线，下个周期重试，否则这批新文章会被永久跳过
        if (sent == 0) {
            log.error("RSS订阅通知一封都没发送成功（订阅人数-{}-），本轮不推进水位线，5分钟后重试，请检查 blog.mail 配置与 SMTP 授权码",
                    subscribers.size());
            return 0;
        }

        saveWatermark(now);
        log.info("RSS订阅通知完成，新文章-{}-篇，成功发送-{}-封", articles.size(), sent);
        return articles.size();
    }

    /**
     * 构建邮件主题
     */
    private String buildNoticeSubject(List<ArticleFrontVO> articles) {
        String siteTitle = rssProperties.getTitle();
        if (articles.size() == 1) {
            return String.format("【%s】新文章：%s", siteTitle, articles.get(0).getTitle());
        }
        return String.format("【%s】有 %d 篇新文章", siteTitle, articles.size());
    }

    /**
     * 构建邮件内容
     */
    private String buildNoticeHtml(RssSubscription subscriber, List<ArticleFrontVO> articles) {
        String siteUrl = trimTrailingSlash(rssProperties.getSiteUrl());
        String unsubscribeUrl = siteUrl + "/rss/unsubscribe?token=" + subscriber.getToken();

        StringBuilder html = new StringBuilder();
        html.append("<div style=\"max-width:640px;margin:0 auto;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',"
                + "'PingFang SC','Microsoft YaHei',sans-serif;color:#303133;\">");
        html.append("<h2 style=\"font-size:18px;margin:0 0 4px;\">")
                .append(escapeXml(rssProperties.getTitle())).append(" 更新啦</h2>");
        html.append("<p style=\"color:#909399;font-size:13px;margin:0 0 20px;\">")
                .append("你好，").append(escapeXml(subscriber.getNickname())).append("，以下是刚发布的文章：</p>");

        for (ArticleFrontVO article : articles) {
            String link = siteUrl + "/article/" + article.getId();
            html.append("<div style=\"padding:16px 18px;margin-bottom:12px;border-radius:10px;background:#f7f8fa;\">");
            html.append("<a href=\"").append(escapeXml(link))
                    .append("\" style=\"font-size:16px;font-weight:600;color:#000000;text-decoration:none;\">")
                    .append(escapeXml(article.getTitle())).append("</a>");
            html.append("<div style=\"color:#909399;font-size:12px;margin:8px 0;\">")
                    .append(escapeXml(formatRfc822(article.getPublishTime())));
            if (StringUtils.hasText(article.getCategory())) {
                html.append(" · ").append(escapeXml(article.getCategory()));
            }
            html.append("</div>");
            if (StringUtils.hasText(article.getSummary())) {
                html.append("<div style=\"font-size:14px;line-height:1.7;color:#606266;\">")
                        .append(escapeXml(article.getSummary())).append("</div>");
            }
            html.append("<div style=\"margin-top:12px;\"><a href=\"").append(escapeXml(link))
                    .append("\" style=\"font-size:13px;color:#409eff;text-decoration:none;\">阅读全文 →</a></div>");
            html.append("</div>");
        }

        html.append("<p style=\"color:#c0c4cc;font-size:12px;line-height:1.8;margin-top:24px;\">")
                .append("你收到这封邮件是因为订阅了本站的新文章通知。<br/>")
                .append("<a href=\"").append(escapeXml(unsubscribeUrl))
                .append("\" style=\"color:#909399;\">不想再收到？点这里退订</a></p>");
        html.append("</div>");
        return html.toString();
    }

    private LocalDateTime readWatermark() {
        String value = systemConfigService.getValue(NOTIFY_WATERMARK_KEY);
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return LocalDateTime.parse(value.trim());
        } catch (Exception e) {
            log.warn("RSS订阅通知水位线格式异常，将重新初始化：{}", value);
            return null;
        }
    }

    private void saveWatermark(LocalDateTime time) {
        systemConfigService.saveValue(NOTIFY_WATERMARK_KEY, time.toString(), "RSS订阅通知已发到的时间");
    }

    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String formatRfc822(LocalDateTime time) {
        if (time == null) {
            return "";
        }
        return time.atZone(ZoneId.of("Asia/Shanghai")).format(RFC822_FORMATTER);
    }

    private String trimTrailingSlash(String url) {
        if (!StringUtils.hasText(url)) {
            return "";
        }
        String result = url.trim();
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    private String escapeXml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
