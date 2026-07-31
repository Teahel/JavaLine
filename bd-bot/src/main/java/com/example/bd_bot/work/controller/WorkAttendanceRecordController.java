package com.example.bd_bot.work.controller;

import com.example.bd_bot.work.dto.WorkAttendanceRecordImportResult;
import com.example.bd_bot.work.dto.WorkOvertimeMonthlyStatResult;
import com.example.bd_bot.work.service.WorkAttendanceRecordService;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Collections;

/**
 * 工作打卡记录。
 *
 * @author ltj
 * @date 2026/7/16 17:00
 */
@RestController
@RequestMapping(path = "/work-attendance-record", name = "工作打卡记录")
public class WorkAttendanceRecordController {

    @Resource
    private WorkAttendanceRecordService workAttendanceRecordService;

    /**
     * 打卡excel导入
     * @param file 文件
     * @return
     */
    @PostMapping("/import")
    public WorkAttendanceRecordImportResult importAttendanceRecords(@RequestParam("file") MultipartFile file) {
        return workAttendanceRecordService.importAttendanceRecords(file);
    }

    /**
     * 按月统计加班时间。
     *
     * @param file 打卡Excel文件
     * @param name 姓名
     * @return 每人每月加班统计
     */
    @PostMapping("/overtime/monthly")
    public WorkOvertimeMonthlyStatResult calculateMonthlyOvertime(@RequestParam("file") MultipartFile file,
                                                                  @RequestParam("name") String name) {
        return workAttendanceRecordService.calculateMonthlyOvertime(file, name);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public WorkOvertimeMonthlyStatResult handleIllegalArgumentException(IllegalArgumentException exception) {
        WorkOvertimeMonthlyStatResult result = new WorkOvertimeMonthlyStatResult();
        result.setCode(500);
        result.setSuccessCount(0);
        result.setErrors(Collections.singletonList(exception.getMessage()));
        return result;
    }

}
