package com.example.bd_bot.work.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.bd_bot.work.entity.HolidayCalendarEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 节假日日历DAO。
 */
@Mapper
public interface HolidayCalendarDao extends BaseMapper<HolidayCalendarEntity> {
}
