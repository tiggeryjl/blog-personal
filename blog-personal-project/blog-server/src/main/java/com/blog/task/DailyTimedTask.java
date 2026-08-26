package com.blog.task;

import com.blog.mapper.DailyMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 日常定时发布任务
 */
@Slf4j
@Component
public class DailyTimedTask {

    @Autowired
    private DailyMapper dailyMapper;

    /**
     * 每分钟执行一次，扫描并发布所有到期的定时日常
     */
    @Scheduled(cron = "0 * * * * ?")
    public void publishExpiredDailies() {
        try {
            int count = dailyMapper.batchPublishExpiredTimed(LocalDateTime.now());
            if (count > 0) {
                log.info("定时任务执行，自动发布了-{}-条日常", count);
            }
        } catch (Exception e) {
            log.error("日常定时发布任务执行异常", e);
        }
    }
}
