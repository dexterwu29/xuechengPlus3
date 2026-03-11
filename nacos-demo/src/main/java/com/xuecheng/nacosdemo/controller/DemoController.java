package com.xuecheng.nacosdemo.controller;

import com.xuecheng.nacosdemo.config.NacosConfigService;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 验证 Nacos 服务注册与配置中心
 * 配置通过 NacosConfigService 的 addListener 实时监听，Nacos 控制台修改后自动生效
 */
@RestController
public class DemoController {

    private final DiscoveryClient discoveryClient;
    private final NacosConfigService nacosConfigService;

    public DemoController(DiscoveryClient discoveryClient, NacosConfigService nacosConfigService) {
        this.discoveryClient = discoveryClient;
        this.nacosConfigService = nacosConfigService;
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("service", "nacos-demo");
        return result;
    }

    /**
     * 验证 Nacos 配置中心：返回从 Nacos 读取的配置
     */
    @GetMapping("/config")
    public Map<String, Object> config() {
        Map<String, Object> result = new HashMap<>();
        result.put("demo.message", nacosConfigService.getDemoMessage());
        result.put("demo.env", nacosConfigService.getDemoEnv());
        result.put("source", "Nacos Config Center (native listener)");
        return result;
    }

    /**
     * 验证 Nacos 服务发现：返回本服务在 Nacos 中的注册信息
     */
    @GetMapping("/discovery")
    public Map<String, Object> discovery() {
        Map<String, Object> result = new HashMap<>();
        List<String> services = discoveryClient.getServices();
        result.put("registeredServices", services);

        List<ServiceInstance> instances = discoveryClient.getInstances("nacos-demo");
        List<Map<String, Object>> instanceList = instances.stream()
                .map(inst -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("serviceId", inst.getServiceId());
                    m.put("host", inst.getHost());
                    m.put("port", inst.getPort());
                    m.put("uri", inst.getUri());
                    return m;
                })
                .collect(Collectors.toList());
        result.put("nacos-demoInstances", instanceList);
        result.put("source", "Nacos Service Discovery");
        return result;
    }
}
