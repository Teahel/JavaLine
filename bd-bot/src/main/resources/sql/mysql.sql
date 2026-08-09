CREATE TABLE `holiday_calendar` (
                                    `id` BIGINT NOT NULL COMMENT '主键ID',
                                    `year` INT NOT NULL COMMENT '年份',
                                    `holiday_date` DATE NOT NULL COMMENT '日期',
                                    `month_day` VARCHAR(10) NOT NULL COMMENT '月日，格式MM-dd',
                                    `holiday` TINYINT(1) NOT NULL COMMENT '是否放假：1放假，0补班',
                                    `name` VARCHAR(50) NOT NULL COMMENT '节假日名称',
                                    `wage` DECIMAL(10, 2) NOT NULL COMMENT '工资倍数',
                                    `rest` INT NULL COMMENT '接口返回的rest值',
                                    `after_flag` TINYINT(1) NULL COMMENT '是否节后补班：1节后，0节前，NULL非补班',
                                    `target` VARCHAR(20) NULL COMMENT '补班对应节假日',
                                    `create_time` DATETIME NULL COMMENT '创建时间',
                                    `update_time` DATETIME NULL COMMENT '更新时间',
                                    PRIMARY KEY (`id`),
                                    KEY `idx_year` (`year`),
                                    KEY `idx_holiday_date` (`holiday_date`),
                                    KEY `idx_year_holiday_date` (`year`, `holiday_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='节假日日期表';


