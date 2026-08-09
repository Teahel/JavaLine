package com.example.bd_bot.work.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.bd_bot.work.dao.HolidayCalendarDao;
import com.example.bd_bot.work.entity.HolidayCalendarEntity;
import com.example.bd_bot.work.service.HolidayCalendarService;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 节假日日历服务实现。
 */
@Service
public class HolidayCalendarServiceImpl implements HolidayCalendarService {

    @Resource
    private HolidayCalendarDao holidayCalendarDao;

    /**
     * 导入指定年份的节假日日历。
     *
     * @param year 年份
     * @param holidayJson 节假日JSON
     * @return 导入数量
     */
    @Override
    public int importHolidayCalendar(Integer year, String holidayJson) {
        if (year == null) {
            throw new IllegalArgumentException("年份不能为空");
        }
        if (!StringUtils.hasText(holidayJson)) {
            throw new IllegalArgumentException("节假日JSON不能为空");
        }

        List<HolidayCalendarEntity> list = parseHolidayCalendar(year, holidayJson);

        holidayCalendarDao.delete(new LambdaQueryWrapper<HolidayCalendarEntity>()
                .eq(HolidayCalendarEntity::getYear, year));
        for (HolidayCalendarEntity entity : list) {
            holidayCalendarDao.insert(entity);
        }

        return list.size();
    }

    /**
     * 查询指定年份的节假日日历。
     *
     * @param year 年份
     * @return 节假日日历列表
     */
    @Override
    public List<HolidayCalendarEntity> listByYear(Integer year) {
        if (year == null) {
            throw new IllegalArgumentException("年份不能为空");
        }
        return holidayCalendarDao.selectList(new LambdaQueryWrapper<HolidayCalendarEntity>()
                .eq(HolidayCalendarEntity::getYear, year)
                .orderByAsc(HolidayCalendarEntity::getHolidayDate));
    }

    private List<HolidayCalendarEntity> parseHolidayCalendar(Integer year, String holidayJson) {
        JsonObject root = JsonParser.parseString(holidayJson).getAsJsonObject();
        JsonObject holidayObject = root.getAsJsonObject("holiday");
        if (holidayObject == null || holidayObject.size() == 0) {
            throw new IllegalArgumentException("节假日JSON缺少holiday节点");
        }

        List<HolidayCalendarEntity> list = new ArrayList<HolidayCalendarEntity>();
        LocalDateTime now = LocalDateTime.now();
        for (Map.Entry<String, JsonElement> entry : holidayObject.entrySet()) {
            String monthDay = entry.getKey();
            JsonObject item = entry.getValue().getAsJsonObject();

            HolidayCalendarEntity entity = new HolidayCalendarEntity();
            entity.setYear(year);
            entity.setMonthDay(monthDay);
            entity.setHolidayDate(LocalDate.parse(getRequiredString(item, "date")));
            entity.setHoliday(getRequiredBoolean(item, "holiday"));
            entity.setName(getRequiredString(item, "name"));
            entity.setWage(getRequiredBigDecimal(item, "wage"));
            entity.setRest(getInteger(item, "rest"));
            entity.setAfterFlag(getBoolean(item, "after"));
            entity.setTarget(getString(item, "target"));
            entity.setCreateTime(now);
            entity.setUpdateTime(now);
            list.add(entity);
        }

        list.sort(Comparator.comparing(HolidayCalendarEntity::getHolidayDate));
        return list;
    }

    private String getRequiredString(JsonObject jsonObject, String fieldName) {
        String value = getString(jsonObject, fieldName);
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException("节假日JSON字段不能为空：" + fieldName);
        }
        return value;
    }

    private String getString(JsonObject jsonObject, String fieldName) {
        JsonElement element = jsonObject.get(fieldName);
        if (element == null || element.isJsonNull()) {
            return null;
        }
        return element.getAsString();
    }

    private Boolean getRequiredBoolean(JsonObject jsonObject, String fieldName) {
        Boolean value = getBoolean(jsonObject, fieldName);
        if (value == null) {
            throw new IllegalArgumentException("节假日JSON字段不能为空：" + fieldName);
        }
        return value;
    }

    private Boolean getBoolean(JsonObject jsonObject, String fieldName) {
        JsonElement element = jsonObject.get(fieldName);
        if (element == null || element.isJsonNull()) {
            return null;
        }
        return element.getAsBoolean();
    }

    private BigDecimal getRequiredBigDecimal(JsonObject jsonObject, String fieldName) {
        JsonElement element = jsonObject.get(fieldName);
        if (element == null || element.isJsonNull()) {
            throw new IllegalArgumentException("节假日JSON字段不能为空：" + fieldName);
        }
        return element.getAsBigDecimal();
    }

    private Integer getInteger(JsonObject jsonObject, String fieldName) {
        JsonElement element = jsonObject.get(fieldName);
        if (element == null || element.isJsonNull()) {
            return null;
        }
        return element.getAsInt();
    }
}
