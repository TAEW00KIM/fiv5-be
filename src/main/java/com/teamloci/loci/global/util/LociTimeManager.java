package com.teamloci.loci.global.util;

import com.teamloci.loci.domain.notification.DailyPushLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@RequiredArgsConstructor
public class LociTimeManager {

    private final TaskScheduler taskScheduler;
    private final DailyPushLogRepository dailyPushLogRepository;
    private final LociPushService lociPushService;

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @Transactional
    public void scheduleDailyLoci() {
        dailyPushLogRepository.truncateTable();

        long randomSeconds = ThreadLocalRandom.current().nextLong(9 * 3600, 22 * 3600);
        LocalDateTime todayLociTime = LocalDateTime.of(LocalDate.now(SEOUL_ZONE), LocalTime.ofSecondOfDay(randomSeconds));

        log.info("📅 오늘의 Loci Time: {}", todayLociTime);

        taskScheduler.schedule(() -> lociPushService.executeGlobalPush(), todayLociTime.atZone(SEOUL_ZONE).toInstant());
    }
}