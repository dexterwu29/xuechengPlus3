# 学成在线 xuechengPlus3

学成在线教育平台第三代升级版，基于 Spring Boot 3.x + Vue 3 微服务架构，实现二代全部功能并引入 AI 能力与现代化技术栈。

## 项目概述

- **一代/二代**：黑马程序员《学成在线》教程版，功能截止到课程发布
- **三代**：在二代基础上进行技术栈升级与架构改造，目标实现二代功能并扩展 AI 与现代化能力

## 目录结构

```
xcplus3/
├── backend/          # 后端微服务（Spring Boot 3.4+ / Java 17）
├── frontend/         # 前端应用（Vue 3 / Node 20+）
├── docs/             # 架构设计、环境部署等文档
└── README.md
```

## 技术栈

| 层级 | 技术选型 |
|------|----------|
| 后端 | Spring Boot 3.4+, Spring Cloud 2024.0.x, Java 17 |
| AI 集成 | Spring AI / LangChain4j（可选） |
| 前端 | Vue 3, Node.js 20+, Vite |
| 注册/配置 | Nacos 2.4+ |
| 消息队列 | RabbitMQ |
| 存储 | MySQL 8, Redis, MinIO, Elasticsearch |

## 开发环境

- 部署环境：腾讯云 4 核 4G 服务器
- 操作系统：CentOS 7.6 + Docker 26
- 详见：[开发环境安装指导](docs/开发环境安装指导-腾讯云CentOS7.6-Docker26.md)

## 快速开始

1. 克隆仓库：`git clone https://github.com/dexterwu29/xuechengPlus3.git`
2. 按 [开发环境安装指导](docs/开发环境安装指导-腾讯云CentOS7.6-Docker26.md) 部署依赖
3. 启动后端：`cd backend && mvn spring-boot:run`（各微服务）
4. 启动前端：`cd frontend && npm install && npm run dev`

## 自动化脚本

### 课程管理接口一键测试（run-tests.py）

课程管理微服务提供 Python 自动化测试脚本，覆盖完整 CRUD 流程、越权防护、分页、审计字段等用例。

**前置条件：**

- 课程管理服务已启动（`xc-plus-content-service`，端口 11001）
- Python 3.x

**用法：**

```bash
cd xc-plus3/backend/xc-plus-content-service/http
python run-tests.py
```

**可选参数：**

- 指定结果目录：`python run-tests.py ./my-results`
- 自定义 BASE_URL：`BASE_URL=http://localhost:11001/content python run-tests.py`

**输出：**

- 结果保存到 `http/results/YYYYMMDD-HHMMSS/*.json`，可逐文件查看
- 终端输出测试结果分析（正常流程通过数、越权防护通过数等）

**测试范围：**

- 分类树、课程 CRUD、师资、营销、教学计划、媒资关联
- 分页、失败用例、越权防护、审计字段（create_by/update_by）

## 相关仓库

- 二代后端：https://github.com/dexterwu29/xuechengPlus2
- 二代前端：https://github.com/dexterwu29/xuechengPlus2-ui

## 许可证

学习与参考用途。
