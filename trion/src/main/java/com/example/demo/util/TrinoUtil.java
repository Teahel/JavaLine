package com.example.demo.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Trino数据库工具类
 * 提供常用的数据库操作方法
 */
@Slf4j
@Component
public class TrinoUtil {

    private final JdbcTemplate trinoJdbcTemplate;

    public TrinoUtil(@Qualifier("trinoJdbcTemplate") JdbcTemplate trinoJdbcTemplate) {
        this.trinoJdbcTemplate = trinoJdbcTemplate;
    }

    /**
     * 执行查询，返回单条记录
     *
     * @param sql SQL语句
     * @param args 参数
     * @return 单条记录Map
     */
    public Map<String, Object> queryForMap(String sql, Object... args) {
        try {
            return trinoJdbcTemplate.queryForMap(sql, args);
        } catch (Exception e) {
            log.error("查询失败: sql={}, args={}", sql, args, e);
            throw new RuntimeException("查询失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行查询，返回多条记录
     *
     * @param sql SQL语句
     * @param args 参数
     * @return 记录列表
     */
    public List<Map<String, Object>> queryForList(String sql, Object... args) {
        try {
            return trinoJdbcTemplate.queryForList(sql, args);
        } catch (Exception e) {
            log.error("查询失败: sql={}, args={}", sql, args, e);
            throw new RuntimeException("查询失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行查询，返回单个值
     *
     * @param sql SQL语句
     * @param args 参数
     * @param <T>  返回类型
     * @return 单个值
     */
    public <T> T queryForObject(String sql, Class<T> requiredType, Object... args) {
        try {
            return trinoJdbcTemplate.queryForObject(sql, requiredType, args);
        } catch (Exception e) {
            log.error("查询失败: sql={}, args={}", sql, args, e);
            throw new RuntimeException("查询失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行查询，返回实体对象列表
     *
     * @param sql  SQL语句
     * @param type 实体类型
     * @param args 参数
     * @param <T>  实体类型
     * @return 实体列表
     */
    public <T> List<T> queryForEntityList(String sql, Class<T> type, Object... args) {
        try {
            RowMapper<T> rowMapper = new BeanPropertyRowMapper<>(type);
            return trinoJdbcTemplate.query(sql, rowMapper, args);
        } catch (Exception e) {
            log.error("查询失败: sql={}, args={}", sql, args, e);
            throw new RuntimeException("查询失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行查询，返回单个实体对象
     *
     * @param sql  SQL语句
     * @param type 实体类型
     * @param args 参数
     * @param <T>  实体类型
     * @return 实体对象
     */
    public <T> T queryForEntity(String sql, Class<T> type, Object... args) {
        try {
            RowMapper<T> rowMapper = new BeanPropertyRowMapper<>(type);
            return trinoJdbcTemplate.queryForObject(sql, rowMapper, args);
        } catch (Exception e) {
            log.error("查询失败: sql={}, args={}", sql, args, e);
            throw new RuntimeException("查询失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行更新操作（INSERT, UPDATE, DELETE）
     * 注意：Trino主要用于查询，不支持传统的事务性DML操作
     *
     * @param sql SQL语句
     * @param args 参数
     * @return 影响行数
     */
    public int update(String sql, Object... args) {
        try {
            return trinoJdbcTemplate.update(sql, args);
        } catch (Exception e) {
            log.error("更新失败: sql={}, args={}", sql, args, e);
            throw new RuntimeException("更新失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行批量更新操作
     *
     * @param sql        SQL语句
     * @param batchArgs 批量参数列表
     * @return 每条语句影响的行数数组
     */
    public int[] batchUpdate(String sql, List<Object[]> batchArgs) {
        try {
            return trinoJdbcTemplate.batchUpdate(sql, batchArgs);
        } catch (Exception e) {
            log.error("批量更新失败: sql={}", sql, e);
            throw new RuntimeException("批量更新失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行任意SQL语句
     *
     * @param sql SQL语句
     * @return 是否执行成功
     */
    public boolean execute(String sql) {
        try {
            return trinoJdbcTemplate.execute((Connection conn) -> {
                try (Statement stmt = conn.createStatement()) {
                    return stmt.execute(sql);
                }
            });
        } catch (Exception e) {
            log.error("执行SQL失败: sql={}", sql, e);
            throw new RuntimeException("执行SQL失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取所有catalog
     *
     * @return catalog列表
     */
    public List<String> getCatalogs() {
        String sql = "SHOW CATALOGS";
        List<Map<String, Object>> result = queryForList(sql);
        List<String> catalogs = new ArrayList<>();
        for (Map<String, Object> row : result) {
            catalogs.add((String) row.get("Catalog"));
        }
        return catalogs;
    }

    /**
     * 获取指定catalog下的所有schema
     *
     * @param catalog catalog名称
     * @return schema列表
     */
    public List<String> getSchemas(String catalog) {
        String sql = "SHOW SCHEMAS FROM " + catalog;
        List<Map<String, Object>> result = queryForList(sql);
        List<String> schemas = new ArrayList<>();
        for (Map<String, Object> row : result) {
            schemas.add((String) row.get("Schema"));
        }
        return schemas;
    }

    /**
     * 获取指定schema下的所有表
     *
     * @param catalog catalog名称
     * @param schema  schema名称
     * @return 表列表
     */
    public List<String> getTables(String catalog, String schema) {
        String sql = "SHOW TABLES FROM " + catalog + "." + schema;
        List<Map<String, Object>> result = queryForList(sql);
        List<String> tables = new ArrayList<>();
        for (Map<String, Object> row : result) {
            tables.add((String) row.get("Table"));
        }
        return tables;
    }

    /**
     * 获取表的所有列信息
     *
     * @param catalog catalog名称
     * @param schema  schema名称
     * @param table   表名
     * @return 列信息列表
     */
    public List<ColumnInfo> getColumns(String catalog, String schema, String table) {
        String sql = "DESCRIBE " + catalog + "." + schema + "." + table;
        List<Map<String, Object>> result = queryForList(sql);
        List<ColumnInfo> columns = new ArrayList<>();
        for (Map<String, Object> row : result) {
            ColumnInfo info = new ColumnInfo();
            info.setColumn((String) row.get("Column"));
            info.setType((String) row.get("Type"));
            info.setExtra((String) row.get("Extra"));
            info.setComment((String) row.get("Comment"));
            columns.add(info);
        }
        return columns;
    }

    /**
     * 测试连接
     *
     * @return 是否连接成功
     */
    public boolean testConnection() {
        try {
            trinoJdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return true;
        } catch (Exception e) {
            log.error("连接测试失败", e);
            return false;
        }
    }

    /**
     * 获取JdbcTemplate实例
     *
     * @return JdbcTemplate
     */
    public JdbcTemplate getJdbcTemplate() {
        return trinoJdbcTemplate;
    }

    /**
     * 列信息实体类
     */
    @lombok.Data
    public static class ColumnInfo {
        private String column;
        private String type;
        private String extra;
        private String comment;
    }
}