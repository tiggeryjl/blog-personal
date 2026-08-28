package com.blog.task;

import com.blog.mapper.ArticleMapper;
import com.blog.mapper.DailyMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 定时任务
 */
@Slf4j
@Component
public class ScheduledTask {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private DailyMapper dailyMapper;

    /**
     * 每分钟执行一次，扫描并发布所有到期的定时文章和日常
     */
    @Scheduled(cron = "0 * * * * ?")
    public void publishExpiredArticles() {
        try {
            int articleCount = articleMapper.batchPublishExpiredTimed(LocalDateTime.now());
            int dailyCount = dailyMapper.batchPublishExpiredTimed(LocalDateTime.now());

            log.info("定时任务执行，自动发布了-{}-个文章,-{}-条日常", articleCount,dailyCount);
        } catch (Exception e) {
            log.error("定时发布任务执行异常", e);
        }
    }
}
