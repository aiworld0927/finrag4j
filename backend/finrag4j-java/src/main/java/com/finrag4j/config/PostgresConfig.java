package com.finrag4j.config;

import com.pgvector.PGvector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * PostgreSQL + PGVector配置类
 * 
 * 功能说明：
 * - 初始化PGVector扩展
 * - 配置向量数据库连接
 * - 支持向量相似度检索
 * 
 * @author FinRag4j Team
 * @version 1.0.0
 */
@Configuration
public class PostgresConfig {

    /**
     * 初始化PGVector扩展
     * - 创建vector类型
     * - 创建ivfflat索引支持
     */
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        
        // 初始化PGVector扩展
        try {
            jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
            System.out.println("PGVector扩展初始化成功");
        } catch (Exception e) {
            System.err.println("PGVector扩展初始化失败: " + e.getMessage());
        }
        
        return jdbcTemplate;
    }
}