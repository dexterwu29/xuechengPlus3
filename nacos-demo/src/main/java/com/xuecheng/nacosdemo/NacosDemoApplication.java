package com.xuecheng.nacosdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Nacos 服务注册与配置中心验证 Demo
 * <p>
 * 验证功能：1. 服务注册到 Nacos  2. 从 Nacos 配置中心读取配置
 */
@SpringBootApplication
@EnableDiscoveryClient
public class NacosDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(NacosDemoApplication.class, args);
    }
}
