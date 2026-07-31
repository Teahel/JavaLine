package com.example.bd_bot.work.controller;

import com.example.bd_bot.work.entity.HolidayCalendarEntity;
import com.example.bd_bot.work.service.HolidayCalendarService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 节假日日历控制器。
 */
@RestController
@RequestMapping(path = "/holiday-calendar", name = "节假日日历")
public class HolidayCalendarController {

    @Resource
    private HolidayCalendarService holidayCalendarService;

    /**
     * 导入指定年份的节假日日历。
     *
     * @param year 年份
     * @param holidayJson 节假日JSON
     * @return 导入数量
     */
    @PostMapping("/import")
    public int importHolidayCalendar(@RequestParam("year") Integer year, @RequestBody String holidayJson) {
        return holidayCalendarService.importHolidayCalendar(year, holidayJson);
    }

    /**
     * 查询指定年份的节假日日历。
     *
     * @param year 年份
     * @return 节假日日历列表
     */
    @GetMapping("/list")
    public List<HolidayCalendarEntity> listByYear(@RequestParam("year") Integer year) {
        return holidayCalendarService.listByYear(year);
    }
}
