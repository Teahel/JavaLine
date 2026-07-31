package com.example.bd_bot.work.service;

import com.example.bd_bot.work.entity.HolidayCalendarEntity;

import java.util.List;

/**
 * 节假日日历服务。
 */
public interface HolidayCalendarService {

    /**
     * 导入指定年份的节假日日历。
     *
     * @param year 年份
     * @param holidayJson 节假日JSON
     * @return 导入数量
     */
    int importHolidayCalendar(Integer year, String holidayJson);

    /**
     * 查询指定年份的节假日日历。
     *
     * @param year 年份
     * @return 节假日日历列表
     */
    List<HolidayCalendarEntity> listByYear(Integer year);
}
