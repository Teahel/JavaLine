package com.example.trion.service.impl;

import com.example.trion.model.PageResult;
import com.example.trion.service.TrinoQueryService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 设备列表查询：先确定哪些设备符合条件，再补充这些设备的展示信息。
 *
 * <p>如果直接把设备、图片、业务系统、发货记录全部 JOIN，一台设备有 3 张图片、
 * 2 个系统时，就可能得到 6 行，导致总数和分页错误。因此查询拆成三步：
 * 统计设备数量 → 查询当前页设备 → 批量补充当前页详情。
 *
 * <p>前提：设备主表中的 (project_code, dev_num) 唯一。
 * 本类消除的是关联表造成的行数放大，不会清理设备主表本身的重复数据。
 */
@Service
public class TrinoQueryServiceImpl implements TrinoQueryService {

    private static final String DEVICE_FROM =
            " FROM bactrino.bd_archives_center.project_device pd";

    /**
     * 给每台设备的发货记录从新到旧编号，rn = 1 就是该设备最新的一条。
     * PARTITION BY 表示“按设备分别编号”；NULLS LAST 表示空时间排在最后。
     * 时间相同则继续按单号、客户和备注排序，使返回内容稳定。
     *
     * <p>这里只能限制设备范围，不能提前按客户、单号或发货时间过滤。
     * 例如先发给 A、后发给 B：必须先选出 B 这条，再判断是否匹配客户 A。
     * 如果先过滤 A 再编号，就会把历史记录误当成最新记录。
     *
     * @param deviceRestriction 内部生成的设备范围 SQL；空串表示对全部设备计算排名
     */
    private static String rankedShipping(String deviceRestriction) {
        return """
                SELECT dtd.dev_code, dt.code, dt.customer, dt.create_time, dt.remark,
                       ROW_NUMBER() OVER (
                           PARTITION BY dtd.dev_code
                           ORDER BY dt.create_time DESC NULLS LAST, dt.code DESC NULLS LAST,
                                    dt.customer DESC NULLS LAST, dt.remark DESC NULLS LAST
                       ) AS rn
                FROM pptrino.platform_producer.delivery_task_device dtd
                JOIN pptrino.platform_producer.delivery_task dt ON dt.code = dtd.task_code
                """ + deviceRestriction;
    }

    private final JdbcTemplate trinoJdbcTemplate;

    public TrinoQueryServiceImpl(@Qualifier("trinoJdbcTemplate") JdbcTemplate trinoJdbcTemplate) {
        this.trinoJdbcTemplate = trinoJdbcTemplate;
    }

    @Override
    public List<String> getCatalogs() {
        return trinoJdbcTemplate.queryForList("SHOW CATALOGS", String.class);
    }

    /**
     * 查询设备列表。筛选条件必须在分页前全部生效，否则可能出现漏设备、页不满的问题。
     * COUNT 和分页共用同一份条件及参数，保证两者采用相同的筛选规则。
     * 无结果或页码超出范围时只查一次 COUNT；非空页执行三条 SQL，不逐台查询。
     */
    @Override
    public PageResult<Map<String, Object>> getDevices(
            String projectName, String projectCode, String devNum, String devName,
            String systemName, String produceCode, String shippingCode, String customer,
            LocalDateTime produceStartTime, LocalDateTime produceEndTime,
            LocalDateTime shippingStartTime, LocalDateTime shippingEndTime,
            int pageNum, int pageSize) {

        // 第一步：组装筛选条件。WHERE 1 = 1 方便后面的可选条件统一追加 AND。
        // SQL 中用 ? 占位，实际值按出现顺序放入 params，避免把用户输入拼进 SQL。
        StringBuilder conditions = new StringBuilder(" WHERE 1 = 1");
        List<Object> params = new ArrayList<>();
        addEqualCondition(conditions, params, "pd.project_code", projectCode);
        addEqualCondition(conditions, params, "pd.dev_num", devNum);
        addEqualCondition(conditions, params, "pd.dev_name", devName);
        // 主表已有的字段直接过滤；项目名称需要到项目表判断。
        // EXISTS 只回答“有没有匹配记录”，不会像 JOIN 一样增加设备行数。
        if (hasText(projectName)) {
            conditions.append("""
                     AND EXISTS (
                        SELECT 1 FROM bactrino.bd_archives_center.project_info pi
                        WHERE pi.project_code = pd.project_code AND pi.project_name = ?)
                    """);
            params.add(projectName);
        }
        // 任一所属系统匹配即可选中设备；详情查询仍展示该设备全部系统。
        if (hasText(systemName)) {
            conditions.append("""
                     AND EXISTS (
                        SELECT 1 FROM bactrino.bd_archives_center.device_business_system_assoc dbsa
                        JOIN bactrino.bd_archives_center.business_system_info bsi
                          ON bsi.id = dbsa.business_system_id
                        WHERE dbsa.dev_num = pd.dev_num AND bsi.name = ?)
                    """);
            params.add(systemName);
        }

        // 生产筛选单独保存：既用于选设备，也用于详情中选出符合条件的生产记录。
        // 生产时间采用设备表的 create_time，而不是生产单的创建时间。
        // 没传生产条件时，COUNT 和分页就不必访问生产表。
        StringBuilder productionConditions = new StringBuilder();
        List<Object> productionParams = new ArrayList<>();
        // 生产单号属于 device_info；即使 produce_info 关联数据缺失，也应能按生产单号查到设备。
        addEqualCondition(productionConditions, productionParams, "di.produce_code", produceCode);
        addTimeConditions(productionConditions, productionParams, "di.create_time",
                produceStartTime, produceEndTime);
        if (!productionConditions.isEmpty()) {
            conditions.append("""
                     AND EXISTS (
                        SELECT 1 FROM pptrino.platform_producer.device_info di
                        JOIN pptrino.platform_producer.produce_info pi2
                          ON pi2.produce_code = di.produce_code
                        WHERE di.dev_code = pd.dev_num
                    """).append(productionConditions).append(")");
            params.addAll(productionParams);
        }

        // WITH 给子查询起一个名字（这里是 ranked_shipping），供后面的 SQL 引用。
        // 只有传了发货条件，COUNT 和分页才需要计算最新发货记录。
        String prefix = "";
        if (hasText(shippingCode) || hasText(customer)
                || shippingStartTime != null || shippingEndTime != null) {
            prefix = "WITH ranked_shipping AS (" + rankedShipping("") + ") ";
            conditions.append("""
                     AND EXISTS (
                        SELECT 1 FROM ranked_shipping dt
                        WHERE dt.dev_code = pd.dev_num AND dt.rn = 1
                    """);
            // 此处已经要求 rn = 1，再追加筛选条件：不能挪进 rankedShipping 内部。
            addEqualCondition(conditions, params, "dt.code", shippingCode);
            addEqualCondition(conditions, params, "dt.customer", customer);
            addTimeConditions(conditions, params, "dt.create_time", shippingStartTime, shippingEndTime);
            conditions.append(")");
        }

        // 第二步：统计符合条件的设备主表行数。
        // 图片、上报时间等只用于展示，不参与 COUNT，减少无关关联的开销。
        Long count = trinoJdbcTemplate.queryForObject(
                prefix + "SELECT COUNT(*)" + DEVICE_FROM + conditions, Long.class, params.toArray());
        long total = count == null ? 0 : count;
        // 例如第 3 页、每页 10 条，需要跳过前 20 条。先转 long 避免 int 乘法溢出。
        long offset = (long) (pageNum - 1) * pageSize;
        if (total == 0 || offset >= total) {
            return new PageResult<>(List.of(), total, pageNum, pageSize);
        }

        // 第三步：只取当前页的主表字段，不在这里关联图片、系统等一对多数据。
        // 创建时间相同时再按项目和设备编码倒序，避免同时间设备的分页顺序不稳定。
        // 仍保留 OFFSET 分页以兼容 pageNum，页码很大时仍可能有深分页开销。
        String pageSql = prefix + """
                SELECT pd.dev_num, pd.project_code, pd.dev_name,
                       pd.create_time AS installation_time,
                       pd.create_by, pd.modify_by, pd.modify_time
                """ + DEVICE_FROM + conditions
                + " ORDER BY pd.create_time DESC NULLS LAST, pd.project_code DESC, pd.dev_num DESC"
                + " OFFSET ? ROWS LIMIT ?";
        // 复制筛选参数后，按 SQL 占位符顺序追加 offset、pageSize，不修改原参数集合。
        List<Object> pageParams = new ArrayList<>(params);
        pageParams.add(offset);
        pageParams.add((long) pageSize);
        List<Map<String, Object>> records = new ArrayList<>();
        for (Map<String, Object> row : trinoJdbcTemplate.queryForList(pageSql, pageParams.toArray())) {
            // 复制成可修改的 Map，后面把图片、系统、发货等字段补充到这一行。
            records.add(new LinkedHashMap<>(row));
        }
        // 第四步：一次批量补齐当前页详情；不在循环里逐台访问数据库。
        // COUNT 与分页是独立查询，期间源数据可能变化，所以这里仍需检查是否为空。
        if (!records.isEmpty()) {
            enrichPage(records, productionConditions.toString(), productionParams);
        }
        return new PageResult<>(records, total, pageNum, pageSize);
    }

    /**
     * 补齐当前页设备的展示字段。每个详情子查询先分组或取第一条，再与页标识关联，
     * 保证每个设备关联键最多匹配一行，避免“图片数量 × 系统数量 × 发货数量”的放大。
     *
     * <p>row_index 是设备在 Java 列表中的下标，仅用于把查询结果放回原位置，
     * 不对外返回，也不是数据库中的设备主键。
     */
    private void enrichPage(List<Map<String, Object>> records,
                            String productionConditions, List<Object> productionParams) {
        // 参数每三个一组：[列表下标, 设备编号, 项目编号]。
        List<Object> params = new ArrayList<>();
        for (int i = 0; i < records.size(); i++) {
            params.add(i);
            params.add(records.get(i).get("dev_num"));
            params.add(records.get(i).get("project_code"));
        }
        // VALUES 把当前页标识组成 SQL 内的小数据集，例如 (0, 'D1', 'P1'), (1, 'D2', 'P1')。
        // 实际传递仍使用 ? 参数；CAST 显式说明各列类型，包括项目编号可能为 null 的情况。
        String pageValues = String.join(", ", Collections.nCopies(records.size(),
                "(CAST(? AS INTEGER), CAST(? AS VARCHAR), CAST(? AS VARCHAR))"));
        // 生产条件的 ? 出现在 VALUES 之后，因此生产参数必须放在页标识参数之后。
        params.addAll(productionParams);

        // WITH 定义的这些命名子查询称为 CTE，并不表示真的创建了临时表。
        // Trino 会内联 CTE，所以传入已经查出的页标识，避免各段详情反复执行原分页 SQL。
        // 各段含义：
        // images：按上传时间给图片编号，最后 JOIN 时只取 rn = 1；其他字段用于同时间排序。
        // systems：按设备分组，array_agg + DISTINCT 去重收集名称，array_sort 排序，
        //          array_join 拼成 "A, B" 字符串；FILTER 排除空名称。
        // projects：按项目编码收敛为一行。MAX 不是“最新项目”，前提是同编码对应同名称。
        // production：生产时间取 device_info.create_time；先应用生产筛选，再从匹配记录中
        //             取最新一条，未筛选时取最新关联记录。
        // shipping：只限制当前页设备，仍对这些设备的全部发货记录排名，不提前过滤历史。
        // concentrator_reports / measure_reports：分别按项目和设备取该来源最大上报时间。
        // 每段 IN (SELECT ... FROM page_keys) 都把详情范围限制在当前页，实际扫描量取决于执行计划。
        String sql = "WITH page_keys(row_index, dev_num, project_code) AS (VALUES " + pageValues + "),"
                + """
                images AS (
                    SELECT dev_no, image_url, longitude, latitude, install_address, upload_time,
                           ROW_NUMBER() OVER (
                               PARTITION BY dev_no
                               ORDER BY upload_time DESC NULLS LAST, image_url DESC NULLS LAST,
                                        install_address DESC NULLS LAST,
                                        longitude DESC NULLS LAST, latitude DESC NULLS LAST
                           ) AS rn
                    FROM bactrino.bd_archives_center.device_image_info
                    WHERE dev_no IN (SELECT dev_num FROM page_keys)
                ), systems AS (
                    SELECT dbsa.dev_num,
                           array_join(array_sort(array_agg(DISTINCT bsi.name)
                               FILTER (WHERE bsi.name IS NOT NULL)), ', ') AS system_name
                    FROM bactrino.bd_archives_center.device_business_system_assoc dbsa
                    JOIN bactrino.bd_archives_center.business_system_info bsi
                      ON bsi.id = dbsa.business_system_id
                    WHERE dbsa.dev_num IN (SELECT dev_num FROM page_keys)
                    GROUP BY dbsa.dev_num
                ), projects AS (
                    SELECT project_code, MAX(project_name) AS project_name
                    FROM bactrino.bd_archives_center.project_info
                    WHERE project_code IN (SELECT project_code FROM page_keys)
                    GROUP BY project_code
                ), device_status AS (
                    -- dev_status_time 一台设备可能有多条状态历史，先按更新时间取最新一条，避免联表后重复设备。
                    SELECT dev_id, dev_status,
                           ROW_NUMBER() OVER (
                               PARTITION BY dev_id
                               ORDER BY modified DESC NULLS LAST, id DESC
                           ) AS rn
                    FROM pptrino.platform_producer.dev_status_time
                    WHERE dev_id IN (
                        SELECT di.id
                        FROM pptrino.platform_producer.device_info di
                        WHERE di.dev_code IN (SELECT dev_num FROM page_keys)
                    )
                ), production AS (
                    -- produce_code 直接取设备表，避免生产单主表缺失时被 LEFT JOIN 置空。
                    -- 产品型号以 code_type 为准，通过生产单的 product_code 关联获取。
                    SELECT di.dev_code, di.produce_code, pi2.product_code, ct.product_type,
                           ds.dev_status, di.create_time AS produce_time,
                           ROW_NUMBER() OVER (
                               PARTITION BY di.dev_code
                               ORDER BY di.create_time DESC NULLS LAST,
                                        di.produce_code DESC NULLS LAST
                           ) AS rn
                    FROM pptrino.platform_producer.device_info di
                    LEFT JOIN pptrino.platform_producer.produce_info pi2
                      ON pi2.produce_code = di.produce_code
                    LEFT JOIN pptrino.platform_producer.code_type ct
                      ON ct.product_code = pi2.product_code
                    LEFT JOIN device_status ds ON ds.dev_id = di.id AND ds.rn = 1
                    WHERE di.dev_code IN (SELECT dev_num FROM page_keys)
                """ + productionConditions + "), shipping AS ("
                + rankedShipping(" WHERE dtd.dev_code IN (SELECT dev_num FROM page_keys)") + "),"
                + """
                concentrator_reports AS (
                    SELECT np.project_code, nc.concentrator_num AS dev_num,
                           MAX(nc.last_report_time) AS last_report_time
                    FROM mctrino.machine_cloud.niot_project np
                    JOIN mctrino.machine_cloud.niot_concentrator nc ON nc.project_id = np.record_id
                    WHERE np.project_code IN (SELECT project_code FROM page_keys)
                      AND nc.concentrator_num IN (SELECT dev_num FROM page_keys)
                    GROUP BY np.project_code, nc.concentrator_num
                ), measure_reports AS (
                    SELECT np.project_code, nm.measure_num AS dev_num,
                           MAX(nm.last_report_time) AS last_report_time
                    FROM mctrino.machine_cloud.niot_project np
                    JOIN mctrino.machine_cloud.niot_measure nm ON nm.project_id = np.record_id
                    WHERE np.project_code IN (SELECT project_code FROM page_keys)
                      AND nm.measure_num IN (SELECT dev_num FROM page_keys)
                    GROUP BY np.project_code, nm.measure_num
                )
                SELECT p.row_index, i.image_url, i.longitude, i.latitude, i.install_address, i.upload_time,
                       pi.project_name, s.system_name, pr.produce_code, pr.product_code, pr.product_type,
                       pr.dev_status, pr.produce_time,
                       sh.code AS shipping_code, sh.customer, sh.create_time AS shipping_time, sh.remark,
                       COALESCE(nc.last_report_time, nm.last_report_time) AS last_report_time
                FROM page_keys p
                LEFT JOIN images i ON i.dev_no = p.dev_num AND i.rn = 1
                LEFT JOIN systems s ON s.dev_num = p.dev_num
                LEFT JOIN projects pi ON pi.project_code = p.project_code
                LEFT JOIN production pr ON pr.dev_code = p.dev_num AND pr.rn = 1
                LEFT JOIN shipping sh ON sh.dev_code = p.dev_num AND sh.rn = 1
                LEFT JOIN concentrator_reports nc
                  ON nc.project_code = p.project_code AND nc.dev_num = pr.dev_code
                LEFT JOIN measure_reports nm
                  ON nm.project_code = p.project_code AND nm.dev_num = pr.dev_code
                """;
        // 最终 LEFT JOIN 保留没有图片、没有发货记录的设备，相应字段返回 null。
        // i/pr/sh 的 rn = 1 在 ON 中限制关联行，不会把无详情的设备过滤掉。
        // COALESCE 保持原规则：集中器时间非空就用它，否则用仪表时间，并非取两者较晚值。
        // 上报信息同时匹配项目编码，且沿用通过生产设备编号关联的规则。
        for (Map<String, Object> detail : trinoJdbcTemplate.queryForList(sql, params.toArray())) {
            // 详情 SQL 不保证返回顺序，用下标定位，保持原设备分页顺序不变。
            int index = ((Number) detail.get("row_index")).intValue();
            Map<String, Object> target = records.get(index);
            detail.forEach((key, value) -> {
                // 内部定位字段不放入接口响应，其他字段补充到对应设备行。
                if (!"row_index".equalsIgnoreCase(key)) {
                    target.put(key, value);
                }
            });
        }
    }

    /** 未传值、空字符串或全空格都视为“不按这个字段筛选”。 */
    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    /**
     * 追加一个可选的等值条件，例如 AND pd.dev_num = ?。
     * column 只能由本类传入固定列名，value 通过 JDBC 参数绑定。
     */
    private static void addEqualCondition(StringBuilder sql, List<Object> params,
                                          String column, String value) {
        if (hasText(value)) {
            sql.append(" AND ").append(column).append(" = ?");
            params.add(value);
        }
    }

    /**
     * 追加左闭右开的时间范围：包含 start，不包含 end；任一端为空就不限制该端。
     * 例如查询 2 月：start = 2 月 1 日零点，end = 3 月 1 日零点。
     * LocalDateTime 转为 JDBC Timestamp 绑定，不在这里进行时区转换。
     */
    private static void addTimeConditions(StringBuilder sql, List<Object> params, String column,
                                          LocalDateTime start, LocalDateTime end) {
        if (start != null) {
            sql.append(" AND ").append(column).append(" >= ?");
            params.add(Timestamp.valueOf(start));
        }
        if (end != null) {
            sql.append(" AND ").append(column).append(" < ?");
            params.add(Timestamp.valueOf(end));
        }
    }
}
