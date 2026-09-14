package com.example.trion.service.impl;

import com.example.trion.model.PageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** Executes the generated queries against fixtures; the adapter only translates SQL dialect. */
public class TrinoQueryServiceImplTest {
    private JdbcTemplate fixture;
    private TrinoQueryServiceImpl service;
    private final List<String> queries = new ArrayList<>();

    @BeforeEach
    void setup() {
        var dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE", "sa", "");
        fixture = new JdbcTemplate(dataSource);
        service = new TrinoQueryServiceImpl(new JdbcTemplate(dataSource) {
            @Override
            public <T> T queryForObject(String sql, Class<T> type, Object... args) {
                queries.add(sql);
                return super.queryForObject(translate(sql), type, args);
            }

            @Override
            public List<Map<String, Object>> queryForList(String sql, Object... args) {
                queries.add(sql);
                return super.queryForList(translate(sql), args);
            }
        });
        for (String schema : List.of("bd_archives_center", "platform_producer", "machine_cloud")) {
            fixture.execute("CREATE SCHEMA " + schema);
        }
        fixture.execute("CREATE ALIAS array_sort FOR '" + getClass().getName() + ".sort'");
        fixture.execute("CREATE ALIAS array_join FOR '" + getClass().getName() + ".join'");
        for (String ddl : List.of(
                "bd_archives_center.project_device(dev_num VARCHAR, project_code VARCHAR, dev_name VARCHAR, "
                        + "create_time TIMESTAMP, create_by VARCHAR, modify_by VARCHAR, modify_time TIMESTAMP)",
                "bd_archives_center.project_info(project_code VARCHAR, project_name VARCHAR)",
                "bd_archives_center.device_image_info(dev_no VARCHAR, image_url VARCHAR, longitude DOUBLE, "
                        + "latitude DOUBLE, install_address VARCHAR, upload_time TIMESTAMP)",
                "bd_archives_center.device_business_system_assoc(dev_num VARCHAR, business_system_id INT)",
                "bd_archives_center.business_system_info(id INT, name VARCHAR)",
                "platform_producer.device_info(id BIGINT, dev_code VARCHAR, produce_code VARCHAR, create_time TIMESTAMP)",
                "platform_producer.produce_info(produce_code VARCHAR, product_code VARCHAR, create_time TIMESTAMP)",
                "platform_producer.code_type(product_code VARCHAR, product_type VARCHAR)",
                "platform_producer.dev_status_time(id BIGINT, dev_id BIGINT, dev_status INT, modified TIMESTAMP)",
                "platform_producer.delivery_task_device(dev_code VARCHAR, task_code VARCHAR)",
                "platform_producer.delivery_task(code VARCHAR, customer VARCHAR, create_time TIMESTAMP, remark VARCHAR)",
                "machine_cloud.niot_project(project_code VARCHAR, record_id INT)",
                "machine_cloud.niot_concentrator(project_id INT, concentrator_num VARCHAR, last_report_time TIMESTAMP)",
                "machine_cloud.niot_measure(project_id INT, measure_num VARCHAR, last_report_time TIMESTAMP)")) {
            fixture.execute("CREATE TABLE " + ddl);
        }
        fixture.update("""
                INSERT INTO bd_archives_center.project_device VALUES
                ('D1', 'P1', 'First', TIMESTAMP '2026-01-01 00:00:00', 'u', 'u', NULL),
                ('D2', 'P1', 'Second', TIMESTAMP '2026-01-01 00:00:00', 'u', 'u', NULL),
                ('D3', 'P2', 'Third', TIMESTAMP '2026-01-01 00:00:00', 'u', 'u', NULL)
                """);
        fixture.update("INSERT INTO bd_archives_center.project_info VALUES ('P1', 'Project 1'), ('P2', 'Project 2')");
        fixture.update("""
                INSERT INTO bd_archives_center.device_image_info VALUES
                ('D1', 'old.jpg', 1, 2, 'old', TIMESTAMP '2026-01-01 00:00:00'),
                ('D1', 'new.jpg', 3, 4, 'new', TIMESTAMP '2026-02-01 00:00:00'),
                ('D1', 'z.jpg', 5, 6, 'tie', TIMESTAMP '2026-02-01 00:00:00'),
                ('D1', 'null-time.jpg', 7, 8, 'unknown', NULL)
                """);
        fixture.update("INSERT INTO bd_archives_center.business_system_info VALUES (1, 'A'), (2, 'B')");
        fixture.update("INSERT INTO bd_archives_center.device_business_system_assoc VALUES ('D1', 2), ('D1', 1), ('D1', 2)");
        fixture.update("""
                INSERT INTO platform_producer.device_info VALUES
                (1, 'D1', 'PR1', TIMESTAMP '2025-01-01 00:00:00'),
                (2, 'D1', 'PR2', TIMESTAMP '2025-02-01 00:00:00')
                """);
        fixture.update("""
                INSERT INTO platform_producer.produce_info VALUES
                ('PR1', 'PC1', TIMESTAMP '2030-01-01 00:00:00'),
                ('PR2', 'PC2', TIMESTAMP '2020-01-01 00:00:00')
                """);
        fixture.update("INSERT INTO platform_producer.code_type VALUES ('PC1', 'Type 1'), ('PC2', 'Type 2')");
        fixture.update("""
                INSERT INTO platform_producer.dev_status_time VALUES
                (1, 2, 2, TIMESTAMP '2025-01-10 00:00:00'),
                (2, 2, 3, TIMESTAMP '2025-02-10 00:00:00')
                """);
        fixture.update("INSERT INTO platform_producer.delivery_task_device VALUES ('D1', 'S1'), ('D1', 'S2'), ('D1', 'S2')");
        fixture.update("""
                INSERT INTO platform_producer.delivery_task VALUES
                ('S1', 'Customer A', TIMESTAMP '2026-01-01 00:00:00', 'old'),
                ('S2', 'Customer B', TIMESTAMP '2026-02-01 00:00:00', 'new')
                """);
        fixture.update("INSERT INTO machine_cloud.niot_project VALUES ('P1', 1), ('P2', 2)");
        fixture.update("""
                INSERT INTO machine_cloud.niot_concentrator VALUES
                (1, 'D1', TIMESTAMP '2026-03-01 00:00:00'),
                (1, 'D1', TIMESTAMP '2026-03-02 00:00:00'),
                (2, 'D1', TIMESTAMP '2026-04-01 00:00:00')
                """);
        fixture.update("INSERT INTO machine_cloud.niot_measure VALUES (1, 'D1', TIMESTAMP '2026-03-03 00:00:00')");
    }

    @Test
    void returnsOneRowPerDeviceWithLatestImageAllSystemsAndLatestShipping() {
        var page = query(null, null, null, null, null, null, null, 1, 10);
        assertThat(page.total()).isEqualTo(3);
        assertThat(page.records()).extracting(r -> r.get("dev_num")).containsExactly("D3", "D2", "D1");
        var device = page.records().get(2);
        assertThat(device).containsEntry("image_url", "z.jpg")
                .containsEntry("install_address", "tie")
                .containsEntry("system_name", "A, B")
                .containsEntry("shipping_code", "S2")
                .containsEntry("customer", "Customer B")
                .containsEntry("produce_code", "PR2")
                .containsEntry("product_code", "PC2")
                .containsEntry("product_type", "Type 2")
                .containsEntry("dev_status", 3);
        assertThat(device.get("produce_time").toString()).isEqualTo("2025-02-01 00:00:00.0");
        assertThat(device.get("last_report_time").toString()).isEqualTo("2026-03-02 00:00:00.0");
        assertThat(device).doesNotContainKey("row_index");
        assertThat(page.records().get(1)).containsEntry("image_url", null).containsEntry("shipping_code", null);
        assertThat(queries).hasSize(3);
        assertThat(queries.get(0)).doesNotContain("JOIN", "pptrino", "mctrino", "device_image_info");
        assertThat(queries.get(2)).doesNotContain("project_device");
    }

    @Test
    void historicalCustomerCodeAndTimeCannotMatch() {
        assertThat(query("Customer A", null, null, null, null, null, null, 1, 10).total()).isZero();
        assertThat(query(null, "S1", null, null, null, null, null, 1, 10).total()).isZero();
        assertThat(query(null, null, null, null, null,
                LocalDateTime.of(2026, 1, 1, 0, 0), LocalDateTime.of(2026, 2, 1, 0, 0), 1, 10).total()).isZero();
    }

    @Test
    void latestShippingMatchesInclusiveStartAndExclusiveEnd() {
        var page = query("Customer B", "S2", null, null, null,
                LocalDateTime.of(2026, 2, 1, 0, 0), LocalDateTime.of(2026, 3, 1, 0, 0), 1, 10);
        assertThat(page.total()).isEqualTo(1);
        assertThat(page.records()).hasSize(1);
        assertThat(page.records().get(0)).containsEntry("shipping_code", "S2");
    }

    @Test
    void filteringOneSystemStillDisplaysAllSystemsAndProductionUsesSameFilters() {
        var page = query(null, null, "B", "PR1", LocalDateTime.of(2025, 2, 1, 0, 0), null, null, 1, 10);
        assertThat(page.total()).isEqualTo(1);
        assertThat(page.records().get(0)).containsEntry("system_name", "A, B").containsEntry("produce_code", "PR1");
    }

    @Test
    void stablePaginationAndOutOfRangeSkipDetails() {
        var second = query(null, null, null, null, null, null, null, 2, 1);
        assertThat(second.total()).isEqualTo(3);
        assertThat(second.records()).extracting(r -> r.get("dev_num")).containsExactly("D2");
        queries.clear();
        var outside = query(null, null, null, null, null, null, null, 4, 1);
        assertThat(outside.total()).isEqualTo(3);
        assertThat(outside.records()).isEmpty();
        assertThat(queries).hasSize(1);
    }

    @Test
    void allBaseFiltersAreBoundAndCombinedBeforePagination() {
        var page = service.getDevices("Project 1", "P1", "D1", "First", "B", "PR1", "S2", "Customer B",
                LocalDateTime.of(2025, 1, 1, 0, 0), LocalDateTime.of(2025, 2, 1, 0, 0),
                LocalDateTime.of(2026, 2, 1, 0, 0), LocalDateTime.of(2026, 3, 1, 0, 0), 1, 10);
        assertThat(page.total()).isEqualTo(1);
        assertThat(page.records()).hasSize(1);
        assertThat(query("' OR 1=1 --", null, null, null, null, null, null, 1, 10).total()).isZero();
    }

    private PageResult<Map<String, Object>> query(String customer, String shippingCode, String system,
            String production, LocalDateTime productionEnd, LocalDateTime shippingStart,
            LocalDateTime shippingEnd, int page, int size) {
        return service.getDevices(null, null, null, null, system, production, shippingCode, customer,
                null, productionEnd, shippingStart, shippingEnd, page, size);
    }

    private static String translate(String sql) {
        return sql.replace("bactrino.", "").replace("pptrino.", "").replace("mctrino.", "")
                .replace("OFFSET ? ROWS LIMIT ?", "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
    }

    // H2 equivalents for Trino's array presentation functions; ranking/filtering SQL is unchanged.
    public static String[] sort(String[] values) {
        if (values == null) return null;
        String[] sorted = values.clone();
        Arrays.sort(sorted, Comparator.nullsLast(Comparator.naturalOrder()));
        return sorted;
    }

    public static String join(String[] values, String separator) {
        return values == null ? null : String.join(separator, values);
    }
}
