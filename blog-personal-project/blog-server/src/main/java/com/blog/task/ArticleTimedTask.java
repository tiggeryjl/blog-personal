package com.blog.task;

import com.blog.mapper.ArticleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class ArticleTimedTask {

    @Autowired
    private ArticleMapper articleMapper;

    /**
     * 每分钟执行一次，扫描并发布所有到期的定时文章
     */
    @Scheduled(cron = "0 * * * * ?")
    public void publishExpiredArticles() {
        try {
            int count = articleMapper.batchPublishExpiredTimed(LocalDateTime.now());
            log.info("定时任务执行，自动发布了-{}-个文章", count);
        } catch (Exception e) {
            log.error("定时发布任务执行异常", e);
        }
    }
}
