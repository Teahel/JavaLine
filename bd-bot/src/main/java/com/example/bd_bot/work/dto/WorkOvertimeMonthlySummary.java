package com.example.bd_bot.work.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 每月加班汇总。
 */
@Data
public class WorkOvertimeMonthlySummary {

    private String name;

    /**
     * 月份，格式 yyyy-MM。
     */
    private String month;

    private Integer totalOvertimeMinutes;

    private BigDecimal totalOvertimeHours;

    private List<WorkOvertimeDailyDetail> details = new ArrayList<WorkOvertimeDailyDetail>();
}
