package com.example.trion.controller;

import com.example.trion.dto.DeviceQueryRequestDto;
import com.example.trion.model.PageResult;
import com.example.trion.service.TrinoQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/trino")
@Tag(name = "Trino 查询", description = "Trino 数据源和设备查询接口")
public class TrinoQueryController {

    private final TrinoQueryService trinoQueryService;

    public TrinoQueryController(
            TrinoQueryService trinoQueryService) {

        this.trinoQueryService = trinoQueryService;
    }

    @GetMapping("/catalogs")
    @Operation(summary = "查询数据源目录")
    public List<String> getCatalogs() {
        return trinoQueryService.getCatalogs();
    }

    @GetMapping("/devices")
    @Operation(summary = "分页查询设备")
    public PageResult<Map<String, Object>> getDevices(
            @ParameterObject
            @ModelAttribute DeviceQueryRequestDto query) {

        if (query.getPageNum() < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "pageNum must be greater than 0");
        }
        if (query.getPageSize() < 1 || query.getPageSize() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "pageSize must be between 1 and 100");
        }

        return trinoQueryService.getDevices(
                query.getProjectName(),
                query.getProjectCode(),
                query.getDevCode(),
                query.getDevName(),
                query.getSystemName(),
                query.getProduceCode(),
                query.getTaskCode(),
                query.getCustomer(),
                query.getCreateStartTime(),
                query.getCreateEndTime(),
                query.getDeliveryStartTime(),
                query.getDeliveryEndTime(),
                query.getPageNum(),
                query.getPageSize());
    }
}
