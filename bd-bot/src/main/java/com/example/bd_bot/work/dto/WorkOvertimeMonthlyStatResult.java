package com.example.bd_bot.work.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 每月加班统计结果。
 */
@Data
public class WorkOvertimeMonthlyStatResult {

    private Integer code = 0;

    private Integer successCount;

    private List<WorkOvertimeMonthlySummary> summaries = new ArrayList<WorkOvertimeMonthlySummary>();

    private List<String> errors = new ArrayList<String>();
}
