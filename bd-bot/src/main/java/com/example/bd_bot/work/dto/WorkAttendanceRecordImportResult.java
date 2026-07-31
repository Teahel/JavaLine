package com.example.bd_bot.work.dto;

import com.example.bd_bot.work.entity.WorkAttendanceRecordEntity;
import lombok.Data;

import java.util.List;

/**
 * 工作打卡记录导入结果。
 */
@Data
public class WorkAttendanceRecordImportResult {

    /**
     * 成功解析的打卡记录数量。
     */
    private int successCount;

    /**
     * 成功解析的打卡记录列表。
     */
    private List<WorkAttendanceRecordEntity> records;

    /**
     * 导入失败的错误信息列表。
     */
    private List<String> errors;

    /**
     * 创建工作打卡记录导入结果。
     *
     * @param successCount 成功解析的打卡记录数量
     * @param records 成功解析的打卡记录列表
     * @param errors 导入失败的错误信息列表
     */
    public WorkAttendanceRecordImportResult(int successCount, List<WorkAttendanceRecordEntity> records,
                                            List<String> errors) {
        this.successCount = successCount;
        this.records = records;
        this.errors = errors;
    }
}
