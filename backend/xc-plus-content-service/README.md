# xc-plus-content-service 课程管理微服务

> 端口：11001 | 基础路径：/content

## 一、环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.x（腾讯云）
- Nacos 2.x（腾讯云）

## 二、启动步骤

### 2.1 创建数据库并执行初始化脚本

```bash
# 在腾讯云 MySQL 中执行
mysql -h <your-mysql-host> -u <user> -p < xc-plus3/backend/xc-plus-content-service/src/main/resources/initSql/xcplus_content.sql
```

或使用 Navicat/DBeaver 等工具执行 `initSql/xcplus_content.sql`。

### 2.2 配置本地环境

1. 复制 `application-example.yml` 为 `application-local.yml`
2. 复制 `bootstrap-example.yml` 为 `bootstrap-local.yml`
3. 在 `application-local.yml` 中填入：
   - MySQL 连接信息（腾讯云）
   - Nacos 地址（腾讯云）
4. 在 `bootstrap-local.yml` 中填入 Nacos 地址

> `application-local.yml` 和 `bootstrap-local.yml` 已加入 .gitignore，不会被版本托管。

### 2.3 启动服务

```bash
cd xc-plus3/backend/xc-plus-content-service
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

或使用 IDE 运行 `ContentServiceApplication`，激活 `local` 配置。

### 2.4 验证

- 健康检查：`http://localhost:11001/content/actuator/health`
- Swagger UI：`http://localhost:11001/content/swagger-ui.html`
- OpenAPI 文档：`http://localhost:11001/content/v3/api-docs`

## 三、接口调用说明

### 3.1 机构隔离

除 `/content/course-categories/tree` 外，所有课程相关接口需在请求头中传入机构 ID：

```
X-Company-Id: 1
```

或

```
X-Company-Id: 2
```

测试机构 ID：`1`、`2`（用于越权测试）。

### 3.2 主要接口

| 模块 | 方法 | 路径 |
|------|------|------|
| 分类 | GET | /content/course-categories/tree |
| 课程 | GET | /content/courses?pageNo=1&pageSize=10 |
| 课程 | POST | /content/courses/page（分页 Body） |
| 课程 | POST | /content/courses（新增） |
| 课程 | GET | /content/courses/{id} |
| 课程 | PUT | /content/courses/{id} |
| 课程 | DELETE | /content/courses/{id} |
| 营销 | GET/PUT | /content/courses/{id}/market |
| 计划 | GET | /content/courses/{id}/teachplans |
| 计划 | POST | /content/courses/{id}/teachplans |
| 计划 | PUT/DELETE | /content/teachplans/{id} |
| 计划 | POST | /content/teachplans/{id}/move-up、move-down |
| 媒资 | POST/DELETE | /content/teachplans/{id}/media |
| 师资 | GET/POST | /content/courses/{id}/teachers |
| 师资 | GET/PUT/DELETE | /content/courses/{id}/teachers/{teacherId} |

## 四、Knife4j / Swagger 测试

1. 打开 `http://localhost:11001/content/swagger-ui.html`
2. 在请求头中添加 `X-Company-Id: 1`
3. 依次测试各接口

## 五、参考文档

- [课程管理模块-开发接口文档](../../docs/课程管理模块-开发接口文档.md)
