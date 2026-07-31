package com.example.demo;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TrinoQueryService {

    private final JdbcTemplate trinoJdbcTemplate;

    public TrinoQueryService(
            @Qualifier("trinoJdbcTemplate")
            JdbcTemplate trinoJdbcTemplate) {

        this.trinoJdbcTemplate = trinoJdbcTemplate;
    }

    /**
     * 查询所有数据源
     */
    public List<String> getCatalogs() {
        return trinoJdbcTemplate.queryForList(
                "SHOW CATALOGS",
                String.class
        );
    }

    /**
     * 查询设备
     */
    public List<Map<String, Object>> getDevices() {
        String sql = """
                       select pd.dev_num ,pd.project_code,pd.dev_name,pd.create_time as installation_time ,pd.create_by,pd.modify_by ,pd.modify_time , dii.image_url ,dii.longitude,dii.latitude,dii.install_address,dii.upload_time,pi.project_name,bsi.name as system_name,pi2.produce_code ,dt.code as shipping_code,pi2.create_time as produce_time ,dt.customer ,dt.create_time as shipping_time,dt.remark,COALESCE(nc.last_report_time, nm.last_report_time) AS last_report_time
                                from mysql201.bd_archives_center.project_device pd
                                left join mysql201.bd_archives_center.device_image_info dii on pd.dev_num = dii.dev_no
                                left join mysql201.bd_archives_center.project_info pi on pi.project_code = pd.project_code
                                left join mysql201.bd_archives_center.device_business_system_assoc dbsa on pd.dev_num = dbsa.dev_num
                                join mysql201.bd_archives_center.business_system_info bsi on bsi.id = dbsa.business_system_id
                                join mysql200.platform_producer.device_info di on di.dev_code  = pd.dev_num
                                JOIN mysql200.platform_producer.produce_info pi2  ON di.produce_code = pi2.produce_code
                                JOIN mysql200.platform_producer.delivery_task_device dtd  ON di.dev_code = dtd.dev_code
                                JOIN mysql200.platform_producer.delivery_task dt ON dtd.task_code = dt.code
                                join mysql202.machine_cloud.niot_project np on np.project_code = pd.project_code
                                LEFT JOIN mysql202.machine_cloud.niot_concentrator nc ON nc.project_id = np.record_id AND nc.concentrator_num = di.dev_code
                 LEFT JOIN mysql202.machine_cloud.niot_measure nm ON nm.project_id = np.record_id AND nm.measure_num = di.dev_code
                 where pi.project_name = '广东工业大学' and pi.project_code = 'bd202512050016' and pd.dev_num ='181001000561' and pd.dev_name = 'trino设备' and bsi.name = '能耗系统' and pi2.produce_code = 'BDLC19-1111-1' and dt.code  = 'BDCH20-04146' and dt.customer = '山东大学' and pi2.create_time >= TIMESTAMP '2010-07-01 00:00:00' AND pi2.create_time < TIMESTAMP '2026-08-01 00:00:00'
                 ORDER BY pd.create_time desc
                                limit 10
                

                """;

        return trinoJdbcTemplate.queryForList(sql);
    }
}
