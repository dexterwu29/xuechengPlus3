package com.xuecheng.nacosdemo.config;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import org.yaml.snakeyaml.Yaml;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 使用 Nacos 原生 ConfigService + addListener 实现配置实时监听
 * 参考 Nacos 官方 ConfigExample：配置变更时 Listener 自动回调，无需手动刷新
 */
@Component
public class NacosConfigService {

    private static final Logger log = LoggerFactory.getLogger(NacosConfigService.class);

    private static final String DATA_ID = "nacos-demo.yaml";
    private static final String GROUP = "DEFAULT_GROUP";

    @Value("${spring.cloud.nacos.config.server-addr:127.0.0.1:8848}")
    private String serverAddr;

    @Value("${spring.cloud.nacos.config.username:nacos}")
    private String username;

    @Value("${spring.cloud.nacos.config.password:}")
    private String password;

    private ConfigService configService;

    /** 当前配置内容，Listener 回调时更新 */
    private final AtomicReference<String> rawContent = new AtomicReference<>("");

    /** 解析后的 demo.message */
    private final AtomicReference<String> demoMessage = new AtomicReference<>("未从Nacos读取到配置");

    /** 解析后的 demo.env */
    private final AtomicReference<String> demoEnv = new AtomicReference<>("local");

    @PostConstruct
    public void init() {
        try {
            Properties props = new Properties();
            props.put(PropertyKeyConst.SERVER_ADDR, serverAddr);
            props.put(PropertyKeyConst.USERNAME, username);
            props.put(PropertyKeyConst.PASSWORD, password);

            configService = NacosFactory.createConfigService(props);

            // 首次拉取
            String content = configService.getConfig(DATA_ID, GROUP, 5000);
            if (content != null && !content.isEmpty()) {
                updateFromContent(content);
                log.info("Nacos 配置首次拉取成功: demo.message={}, demo.env={}", demoMessage.get(), demoEnv.get());
            }

            // 添加监听器：Nacos 配置变更时自动回调
            configService.addListener(DATA_ID, GROUP, new Listener() {
                @Override
                public void receiveConfigInfo(String configInfo) {
                    log.info("收到 Nacos 配置变更通知，新内容长度: {}", configInfo != null ? configInfo.length() : 0);
                    if (configInfo != null && !configInfo.isEmpty()) {
                        updateFromContent(configInfo);
                        log.info("配置已更新: demo.message={}, demo.env={}", demoMessage.get(), demoEnv.get());
                    }
                }

                @Override
                public Executor getExecutor() {
                    return null;
                }
            });
            log.info("Nacos 配置监听器已注册，dataId={}, group={}", DATA_ID, GROUP);

        } catch (NacosException e) {
            log.error("Nacos ConfigService 初始化失败", e);
        }
    }

    private void updateFromContent(String content) {
        rawContent.set(content);
        try {
            Yaml yaml = new Yaml();
            Object loaded = yaml.load(content);
            if (loaded instanceof Map<?, ?> root) {
                Object demoObj = root.get("demo");
                if (demoObj instanceof Map<?, ?> demo) {
                    Object msg = demo.get("message");
                    Object env = demo.get("env");
                    if (msg != null) demoMessage.set(String.valueOf(msg));
                    if (env != null) demoEnv.set(String.valueOf(env));
                }
            }
        } catch (Exception e) {
            log.warn("解析 Nacos 配置失败，保留旧值", e);
        }
    }

    public String getDemoMessage() {
        return demoMessage.get();
    }

    public String getDemoEnv() {
        return demoEnv.get();
    }
}
