package com.example.bd_bot.work.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * 工作打卡记录实体类。
 */
@Data
@TableName("work_attendance_record")
public class WorkAttendanceRecordEntity {

    /**
     * 主键ID。
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 打卡人员姓名。
     */
    @TableField("name")
    private String name;

    /**
     * 打卡日期。
     */
    @TableField("attendance_date")
    private LocalDate attendanceDate;

    /**
     * 打卡时间。
     */
    @TableField("attendance_time")
    private LocalTime attendanceTime;

    /**
     * 创建时间。
     */
    @TableField("create_time")
    private LocalDateTime createTime;

}
