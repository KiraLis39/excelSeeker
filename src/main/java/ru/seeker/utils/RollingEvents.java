package ru.seeker.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import ru.seeker.config.Constant;
import ru.seeker.service.WordSearchService;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@ConditionalOnProperty(name = "scheduler.enabled", matchIfMissing = true)
public class RollingEvents {
    private final WordSearchService wordSearchService;

    //    @Async
//    @Scheduled(initialDelay = 6_000, fixedRate = Constant.NEXT_STATUS_SWITCH_MINUTES * 60 * 1_000)
//    public void autoStatusSwitcher() {
//        try {
//            log.info("Scheduled check for update events statuses...");
//            log.info("Scheduled check for update events statuses accomplished.");
//        } catch (Exception e) {
//            log.info("Scheduled check for update events statuses failed: {}", ExceptionUtils.getFullExceptionMessage(e));
//        }
//    }
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(Constant.SCHEDULER_POOL_SIZE);
        threadPoolTaskScheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
        return threadPoolTaskScheduler;
    }
}
