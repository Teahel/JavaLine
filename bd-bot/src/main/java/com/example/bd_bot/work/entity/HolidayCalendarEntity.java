package com.example.bd_bot.work.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 节假日日历实体类。
 */
@Data
@TableName("holiday_calendar")
public class HolidayCalendarEntity {

    /**
     * 主键ID。
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 年份。
     */
    @TableField("year")
    private Integer year;

    /**
     * 日期。
     */
    @TableField("holiday_date")
    private LocalDate holidayDate;

    /**
     * 月日，格式MM-dd。
     */
    @TableField("month_day")
    private String monthDay;

    /**
     * 是否放假：true放假，false补班。
     */
    @TableField("holiday")
    private Boolean holiday;

    /**
     * 节假日名称。
     */
    @TableField("name")
    private String name;

    /**
     * 工资倍数。
     */
    @TableField("wage")
    private BigDecimal wage;

    /**
     * 接口返回的rest值。
     */
    @TableField("rest")
    private Integer rest;

    /**
     * 是否节后补班：true节后，false节前，null非补班。
     */
    @TableField("after_flag")
    private Boolean afterFlag;

    /**
     * 补班对应节假日。
     */
    @TableField("target")
    private String target;

    /**
     * 创建时间。
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间。
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

}
