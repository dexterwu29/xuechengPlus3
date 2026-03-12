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
- Knife4j 文档：`http://localhost:11001/content/doc.html`
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

### 4.1 接口文档访问排查

**访问失败原因与排查：**

- **现象**：访问 `http://localhost:11001/content/doc.html` 返回 403 或 404
- **原因**：机构隔离拦截器（`CompanyInterceptor`）对 `/courses/**`、`/teachplans/**` 等路径生效，若配置不当可能误拦截 `/doc.html`、`/v3/api-docs` 等文档路径
- **解决**：采用**白名单**策略，仅对业务 API 生效，文档路径放行：
  - `WebMvcConfig` 中 `addPathPatterns("/courses/**", "/teachplans/**")`，不包含 `/doc.html`、`/v3/api-docs`、`/swagger-ui/**`
  - 对 `GlobalExceptionHandler` 使用 `@Hidden` 或排除，避免 SpringDoc 扫描异常类导致文档生成失败

**访问成功条件：**

- 服务启动后，`/content/doc.html`、`/content/v3/api-docs` 未被拦截器拦截
- Knife4j 与 SpringDoc 正确集成，`knife4j.enable: true` 已配置

### 4.2 接口测试文档

- [12-课程管理模块-接口测试用例](../../docs/12-课程管理模块-接口测试用例.md)：每个接口的 JSON 请求示例、成功/失败用例、curl 命令，可直接复制粘贴测试

### 4.3 一键 HTTP 测试

| 方式 | 说明 |
|------|------|
| **IDEA HTTP Client** | 打开 `http/课程管理接口测试.http`，点击 **Run all requests** 一键执行全部用例，结果在 Run 窗口查看 |
| **Python 自动化脚本** | `cd http && python run-tests.py`，结果保存到 `http/results/YYYYMMDD-HHMMSS/*.json`，可逐文件查看。详见项目根目录 README 的「自动化脚本」章节 |

## 五、参考文档

- [课程管理模块-开发接口文档](../../docs/11-课程管理模块-开发接口文档.md)
