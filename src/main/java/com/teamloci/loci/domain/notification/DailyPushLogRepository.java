package com.teamloci.loci.domain.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface DailyPushLogRepository extends JpaRepository<DailyPushLog, String> {

    @Modifying
    @Query(value = "TRUNCATE TABLE daily_push_logs", nativeQuery = true)
    void truncateTable();

    @Query("SELECT d.userId FROM DailyPushLog d")
    List<Long> findAllUserIds();

    boolean existsByDate(LocalDate date);
}