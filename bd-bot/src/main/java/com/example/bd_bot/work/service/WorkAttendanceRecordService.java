package com.example.bd_bot.work.service;

import com.example.bd_bot.work.dto.WorkAttendanceRecordImportResult;
import com.example.bd_bot.work.dto.WorkOvertimeMonthlyStatResult;
import org.springframework.web.multipart.MultipartFile;

public interface WorkAttendanceRecordService {

    /**
     * 打卡excel导入
     * @param file 文件
     * @return
     */
    WorkAttendanceRecordImportResult importAttendanceRecords(MultipartFile file);

    /**
     * 按月统计加班时间。
     *
     * @param file 打卡Excel文件
     * @param name 姓名
     * @return 每人每月加班统计
     */
    WorkOvertimeMonthlyStatResult calculateMonthlyOvertime(MultipartFile file, String name);
}
