package com.finrag4j.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j接口文档配置类
 * 
 * 功能说明：
 * - 配置Swagger/OpenAPI接口文档
 * - 提供在线接口测试功能
 * - 生成API文档供前后端对接
 * 
 * 访问地址: http://localhost:8080/doc.html
 * 
 * @author FinRag4j Team
 * @version 1.0.0
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FinRag4j API接口文档")
                        .version("1.0.0")
                        .description("FinRag4j - 面向金融行业的企业级大模型RAG应用框架")
                        .contact(new Contact()
                                .name("FinRag4j Team")
                                .email("support@finrag4j.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}