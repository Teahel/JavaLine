package com.example.bd_bot.work.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 每日加班明细。
 */
@Data
public class WorkOvertimeDailyDetail {

    private String name;

    private LocalDate attendanceDate;

    private LocalTime firstPunchTime;

    private LocalTime lastPunchTime;

    private Boolean workday;

    private Boolean holiday;

    private String holidayName;

    private Integer overtimeMinutes;

    private BigDecimal overtimeHours;
}
