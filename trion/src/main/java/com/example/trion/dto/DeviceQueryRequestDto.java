package com.example.trion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Schema(description = "设备分页查询条件")
public class DeviceQueryRequestDto {

    @Schema(description = "项目名称")
    private String projectName;

    @Schema(description = "项目编码")
    private String projectCode;

    @Schema(description = "设备编码")
    private String devCode;

    @Schema(description = "设备名称")
    private String devName;

    @Schema(description = "系统名称")
    private String systemName;

    @Schema(description = "生产编号")
    private String produceCode;

    @Schema(description = "发货单号")
    private String taskCode;

    @Schema(description = "客户名称")
    private String customer;

    @Schema(description = "生产开始时间，格式：yyyy-MM-dd HH:mm:ss", type = "string")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createStartTime;

    @Schema(description = "生产结束时间，格式：yyyy-MM-dd HH:mm:ss", type = "string")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createEndTime;

    @Schema(description = "发货开始时间，格式：yyyy-MM-dd HH:mm:ss", type = "string")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deliveryStartTime;

    @Schema(description = "发货结束时间，格式：yyyy-MM-dd HH:mm:ss", type = "string")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deliveryEndTime;

    @Schema(description = "页码，从 1 开始", defaultValue = "1", minimum = "1")
    private int pageNum = 1;

    @Schema(description = "每页条数", defaultValue = "10", minimum = "1", maximum = "100")
    private int pageSize = 10;
}
