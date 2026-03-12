package com.xuecheng.content.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 配置 - server basePath、全局请求头 X-Company-Id（默认 1，Knife4j 初次访问即可测试）
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("课程管理 API").version("1.0"))
                .servers(List.of(new Server().url(contextPath)));
    }

    /** 为所有接口添加 X-Company-Id 全局请求头，默认值 1，Knife4j 初次访问即可测试 */
    @Bean
    public GlobalOpenApiCustomizer globalOpenApiCustomizer() {
        return openApi -> openApi.getPaths().values().forEach(pathItem ->
                pathItem.readOperations().forEach(op -> {
                    Parameter header = new Parameter()
                            .in("header")
                            .name("X-Company-Id")
                            .description("机构 ID，测试可用 1 或 2")
                            .required(true)
                            .schema(new StringSchema()._default("1"));
                    op.addParametersItem(header);
                }));
    }
}
