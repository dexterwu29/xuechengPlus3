# Nacos Demo - 服务注册与配置中心验证

> 用于验证学成在线三代微服务架构中 Nacos 的**服务注册**与**配置中心**功能。

## 技术栈

- Java 17
- Spring Boot 3.4.3
- Spring Cloud 2024.0.1
- Spring Cloud Alibaba 2023.0.1.2
- Nacos 2.4.0

## 前置条件

1. 已按 [开发环境安装指导](./docs/开发环境安装指导-最小化2.0-本地版.md) 第十章完成 Nacos 安装
2. Nacos 控制台可访问：http://你的服务器IP:8848/nacos

## 本地配置（首次运行必做）

**所有本地配置不得提交到 Git。**

### 1. 复制并填写 Nacos 地址

```bash
cd nacos-demo/src/main/resources
cp application-example.yml application-local.yml
cp bootstrap-example.yml bootstrap-local.yml
# 编辑上述两个文件，将「你的Nacos服务器IP」替换为实际地址
```

示例：若 Nacos 服务器 IP 为 `192.168.1.100`，则填写：

```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: 192.168.1.100:8848
      config:
        server-addr: 192.168.1.100:8848
```

若 Nacos 开启鉴权，取消 `username`、`password` 注释并填入。

### 2. 在 Nacos 控制台添加配置（验证配置中心）

在 Nacos 控制台 → **配置管理** → **配置列表** → **新建配置**：

| 配置项 | 值 |
|--------|-----|
| Data ID | `nacos-demo.yaml` |
| Group | `DEFAULT_GROUP` |
| 配置格式 | YAML |

配置内容示例：

```yaml
demo:
  message: "Hello from Nacos Config Center!"
  env: dev
```

保存后，应用启动时会从 Nacos 拉取该配置。

## 启动

```bash
cd nacos-demo
mvn spring-boot:run
```

或指定 profile（若本地配置在 application-local.yml）：

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

## 验证接口

| 接口 | 说明 |
|------|------|
| http://localhost:8081/health | 健康检查 |
| http://localhost:8081/config | 验证 Nacos 配置中心：返回从 Nacos 读取的 `demo.message`、`demo.env` |
| http://localhost:8081/discovery | 验证 Nacos 服务发现：返回已注册服务列表及 `nacos-demo` 实例信息 |

### 预期结果

- **/config**：返回 `demo.message` 和 `demo.env` 为 Nacos 中配置的值
- **/discovery**：在 Nacos 控制台 **服务管理** → **服务列表** 中可看到 `nacos-demo` 服务

## 目录结构

```
nacos-demo/
├── pom.xml
├── README.md
└── src/main/
    ├── java/com/xuecheng/nacosdemo/
    │   ├── NacosDemoApplication.java
    │   └── controller/DemoController.java
    └── resources/
        ├── application.yml
        ├── application-example.yml
        ├── application-local.yml  # 本地配置，不提交
        ├── bootstrap.yml
        ├── bootstrap-example.yml
        └── bootstrap-local.yml    # 本地配置，不提交
```

## 故障排查

1. **连接不到 Nacos**：检查 `application-local.yml` 中的 `server-addr` 是否正确，防火墙是否放行 8848、9848
2. **配置未生效**：确认 Nacos 中已创建 `nacos-demo.yaml`，Data ID 与 Group 与配置一致
3. **Nacos 2.4.0 鉴权**：若开启鉴权，需在控制台首次访问时创建管理员用户，并在 `application-local.yml`、`bootstrap-local.yml` 中配置 `username`、`password`
4. **MalformedInputException / 配置解析失败**：Nacos 配置内容需为 **UTF-8** 编码。若含中文导致解析失败，可暂时改用纯 ASCII，例如 `message: "Hello from Nacos [realtime]"`
