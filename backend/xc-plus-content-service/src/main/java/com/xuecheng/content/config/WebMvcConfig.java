package com.xuecheng.content.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置 - 注册机构隔离拦截器
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final CompanyInterceptor companyInterceptor;

    /**
     * 机构隔离拦截器：仅对业务 API 生效（白名单），Knife4j/doc.html/v3/api-docs 等自动放行
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(companyInterceptor)
                .addPathPatterns("/courses/**", "/teachplans/**");
    }
}
