package com.example.trion.service;

import com.example.trion.model.PageResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface TrinoQueryService {

    /**
     * 查询所有数据源
     */
    List<String> getCatalogs();

    /**
     * 查询设备
     */
    PageResult<Map<String, Object>> getDevices(
            String projectName,
            String projectCode,
            String devNum,
            String devName,
            String systemName,
            String produceCode,
            String shippingCode,
            String customer,
            LocalDateTime produceStartTime,
            LocalDateTime produceEndTime,
            LocalDateTime shippingStartTime,
            LocalDateTime shippingEndTime,
            int pageNum,
            int pageSize);
}
