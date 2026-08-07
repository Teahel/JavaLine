package com.example.trion.service.impl;

import com.example.trion.model.PageResult;
import com.example.trion.service.TrinoQueryService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TrinoQueryServiceImpl implements TrinoQueryService {

    private final JdbcTemplate trinoJdbcTemplate;

    public TrinoQueryServiceImpl(
            @Qualifier("trinoJdbcTemplate")
            JdbcTemplate trinoJdbcTemplate) {

        this.trinoJdbcTemplate = trinoJdbcTemplate;
    }

    @Override
    public List<String> getCatalogs() {
        return trinoJdbcTemplate.queryForList(
                "SHOW CATALOGS",
                String.class
        );
    }

    @Override
    public PageResult<Map<String, Object>> getDevices(
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
            int pageSize) {

        String selectClause = """
                SELECT pd.dev_num,
                       pd.project_code,
                       pd.dev_name,
                       pd.create_time AS installation_time,
                       pd.create_by, 
                       pd.modify_by, 
                       pd.modify_time,
                       dii.image_url,
                       dii.longitude,
                       dii.latitude,
                       dii.install_address,
                       dii.upload_time,
                       pi.project_name, 
                       bsi.name AS system_name,
                       pi2.produce_code, 
                       dt.code AS shipping_code,
                       pi2.create_time AS produce_time,
                       dt.customer,
                       dt.create_time AS shipping_time, 
                       dt.remark,
                       COALESCE(nc.last_report_time, nm.last_report_time) AS last_report_time
                """;
        String fromClause = """
                FROM mysql201.bd_archives_center.project_device pd
                LEFT JOIN mysql201.bd_archives_center.device_image_info dii
                       ON pd.dev_num = dii.dev_no
                LEFT JOIN mysql201.bd_archives_center.project_info pi
                       ON pi.project_code = pd.project_code
                LEFT JOIN mysql201.bd_archives_center.device_business_system_assoc dbsa
                       ON pd.dev_num = dbsa.dev_num
                JOIN mysql201.bd_archives_center.business_system_info bsi
                     ON bsi.id = dbsa.business_system_id
                JOIN mysql200.platform_producer.device_info di
                     ON di.dev_code = pd.dev_num
                JOIN mysql200.platform_producer.produce_info pi2
                     ON di.produce_code = pi2.produce_code
                JOIN mysql200.platform_producer.delivery_task_device dtd
                     ON di.dev_code = dtd.dev_code
                JOIN mysql200.platform_producer.delivery_task dt
                     ON dtd.task_code = dt.code
                JOIN mysql202.machine_cloud.niot_project np
                     ON np.project_code = pd.project_code
                LEFT JOIN mysql202.machine_cloud.niot_concentrator nc
                       ON nc.project_id = np.record_id
                      AND nc.concentrator_num = di.dev_code
                LEFT JOIN mysql202.machine_cloud.niot_measure nm
                       ON nm.project_id = np.record_id
                      AND nm.measure_num = di.dev_code
                """;

        StringBuilder conditions = new StringBuilder(" WHERE 1 = 1");
        List<Object> params = new ArrayList<>();

        addEqualCondition(conditions, params, "pi.project_name", projectName);
        addEqualCondition(conditions, params, "pi.project_code", projectCode);
        addEqualCondition(conditions, params, "pd.dev_num", devNum);
        addEqualCondition(conditions, params, "pd.dev_name", devName);
        addEqualCondition(conditions, params, "bsi.name", systemName);
        addEqualCondition(conditions, params, "pi2.produce_code", produceCode);
        addEqualCondition(conditions, params, "dt.code", shippingCode);
        addEqualCondition(conditions, params, "dt.customer", customer);

        if (produceStartTime != null) {
            conditions.append(" AND pi2.create_time >= ?");
            params.add(Timestamp.valueOf(produceStartTime));
        }
        if (produceEndTime != null) {
            conditions.append(" AND pi2.create_time < ?");
            params.add(Timestamp.valueOf(produceEndTime));
        }
        if (shippingStartTime != null) {
            conditions.append(" AND dt.create_time >= ?");
            params.add(Timestamp.valueOf(shippingStartTime));
        }
        if (shippingEndTime != null) {
            conditions.append(" AND dt.create_time < ?");
            params.add(Timestamp.valueOf(shippingEndTime));
        }

        String countSql = "SELECT COUNT(*) " + fromClause + conditions;
        Long total = trinoJdbcTemplate.queryForObject(
                countSql,
                Long.class,
                params.toArray());

        long offset = (long) (pageNum - 1) * pageSize;
        String dataSql = selectClause + fromClause + conditions
                + " ORDER BY pd.create_time DESC OFFSET ? ROWS LIMIT ?";
        List<Object> dataParams = new ArrayList<>(params);
        dataParams.add(offset);
        dataParams.add((long) pageSize);

        List<Map<String, Object>> records = trinoJdbcTemplate.queryForList(
                dataSql,
                dataParams.toArray());
        return new PageResult<>(
                records,
                total == null ? 0 : total,
                pageNum,
                pageSize);
    }

    private void addEqualCondition(
            StringBuilder query,
            List<Object> params,
            String column,
            String value) {
        if (value != null && !value.isBlank()) {
            query.append(" AND ").append(column).append(" = ?");
            params.add(value);
        }
    }
}
