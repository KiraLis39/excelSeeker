package ru.seeker.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import ru.seeker.config.Constant;
import ru.seeker.service.ParseService;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@ConditionalOnProperty(name = "scheduler.enabled", matchIfMissing = true)
public class RollingEvents {
    private final ParseService parseService;

    @Async
    @Scheduled(initialDelay = 10_000, fixedRate = Constant.ONCE_PER_DAY_HOURS * 60 * 60 * 1_000)
    public void autoSwitchOffOldSurveys() {
        try {
            log.info("Авто-загрузка csv номенклатуры из 'Тор'...");
            parseService.reloadTorData();
            log.info("Авто-загрузка csv номенклатуры из 'Тор' завершена.");
        } catch (Exception e) {
            log.warn("Авто-загрузка csv номенклатуры из 'Тор' провалилась: {}", ExceptionUtils.getFullExceptionMessage(e));
        }
    }

    @Async
    @Scheduled(initialDelay = 20_000, fixedRate = Constant.ONCE_PER_DAY_HOURS * 60 * 60 * 1_000)
    public void fillLostRespondentsRegisteredDate() {
        try {
            log.info("Авто-загрузка json номенклатуры из 'Сварка ПТК'...");
            parseService.reloadPtkData();
            log.info("Авто-загрузка json номенклатуры из 'Сварка ПТК' завершена.");
        } catch (Exception e) {
            log.error("Авто-загрузка json номенклатуры из 'Сварка ПТК' провалилась: {}", ExceptionUtils.getFullExceptionMessage(e));
        }
    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(Constant.SCHEDULER_POOL_SIZE);
        threadPoolTaskScheduler.setThreadGroupName("Schedulers");
        threadPoolTaskScheduler.setThreadNamePrefix("SeekerScheduler_");
        threadPoolTaskScheduler.setErrorHandler(t -> log.warn("Ошибка в Шедулере: {}", ExceptionUtils.getFullExceptionMessage(t)));
        threadPoolTaskScheduler.setDaemon(true);
        return threadPoolTaskScheduler;
    }
}
