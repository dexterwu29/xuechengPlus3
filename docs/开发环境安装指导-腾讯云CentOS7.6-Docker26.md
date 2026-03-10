# 学成在线三代 开发环境安装指导

适用于腾讯云 **CentOS 7.6 + Docker 26.1.3** 环境（4 核 4G，20GB 系统盘）。

## 一、环境概览

| 二代（黑马虚拟机 192.168.101.65） | 三代（腾讯云） |
|----------------------------------|----------------|
| xxl-job-admin:2.3.1 | xxl-job:2.4+ |
| minio:RELEASE.2022-09-07 | minio:2024.x |
| redis:6.2.7 | redis:7.2 |
| elasticsearch:7.12.1 | elasticsearch:8.11 |
| gogs | gogs 或 Gitea |
| rabbitmq:3.8.34 | rabbitmq:3.13 |
| nacos-server:1.4.1 | nacos:2.4 |
| mysql:8.0.26 | mysql:8.0 |

---

## 二、前置准备

### 2.1 系统与 Docker

腾讯云镜像 **CentOS7.6-Docker26** 已预装 Docker 26.1.3，确认版本：

```bash
docker --version
# Docker version 26.1.3, build ...
```

### 2.2 创建数据目录

```bash
sudo mkdir -p /data/docker/{mysql,redis,minio,elasticsearch,nacos,rabbitmq,xxl-job,gogs}
sudo chown -R $(whoami):$(whoami) /data/docker
```

---

## 三、Docker Compose 一键部署

### 3.1 安装 Docker Compose

```bash
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
docker-compose --version
```

### 3.2 创建 `docker-compose.yml`

在 `/data` 或项目目录创建：

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: xcplus3-mysql
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD:-your_password}
      MYSQL_DATABASE: xcplus
    ports:
      - "3306:3306"
    volumes:
      - /data/docker/mysql:/var/lib/mysql
    command: --default-authentication-plugin=mysql_native_password --character-set-server=utf8mb4
    networks:
      - xcplus3-net

  redis:
    image: redis:7.2-alpine
    container_name: xcplus3-redis
    restart: unless-stopped
    ports:
      - "6379:6379"
    volumes:
      - /data/docker/redis:/data
    command: redis-server --appendonly yes
    networks:
      - xcplus3-net

  nacos:
    image: nacos/nacos-server:v2.4.0
    container_name: xcplus3-nacos
    restart: unless-stopped
    environment:
      MODE: standalone
      SPRING_DATASOURCE_PLATFORM: mysql
      MYSQL_SERVICE_HOST: mysql
      MYSQL_SERVICE_DB_NAME: nacos_config
      MYSQL_SERVICE_USER: root
      MYSQL_SERVICE_PASSWORD: ${MYSQL_ROOT_PASSWORD:-your_password}
      JVM_XMS: 256m
      JVM_XMX: 256m
    ports:
      - "8848:8848"
      - "9848:9848"
    depends_on:
      - mysql
    networks:
      - xcplus3-net

  rabbitmq:
    image: rabbitmq:3.13-management-alpine
    container_name: xcplus3-rabbitmq
    restart: unless-stopped
    environment:
      RABBITMQ_DEFAULT_USER: admin
      RABBITMQ_DEFAULT_PASS: ${RABBITMQ_PASS:-admin123}
    ports:
      - "5672:5672"
      - "15672:15672"
    volumes:
      - /data/docker/rabbitmq:/var/lib/rabbitmq
    networks:
      - xcplus3-net

  minio:
    image: minio/minio:RELEASE.2024-01-16T16-07-38Z
    container_name: xcplus3-minio
    restart: unless-stopped
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: ${MINIO_PASS:-minioadmin123}
    ports:
      - "9000:9000"
      - "9001:9001"
    volumes:
      - /data/docker/minio:/data
    command: server /data --console-address ":9001"
    networks:
      - xcplus3-net

  elasticsearch:
    image: elasticsearch:8.11.0
    container_name: xcplus3-elasticsearch
    restart: unless-stopped
    environment:
      discovery.type: single-node
      xpack.security.enabled: "false"
      "ES_JAVA_OPTS": "-Xms512m -Xmx512m"
    ports:
      - "9200:9200"
      - "9300:9300"
    volumes:
      - /data/docker/elasticsearch:/usr/share/elasticsearch/data
    networks:
      - xcplus3-net

  xxl-job:
    image: xuxueli/xxl-job-admin:2.4.2
    container_name: xcplus3-xxl-job
    restart: unless-stopped
    environment:
      PARAMS: "--spring.datasource.url=jdbc:mysql://mysql:3306/xxl_job?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai --spring.datasource.username=root --spring.datasource.password=${MYSQL_ROOT_PASSWORD:-your_password}"
    ports:
      - "18088:8080"
    depends_on:
      - mysql
    networks:
      - xcplus3-net

  gogs:
    image: gogs/gogs:latest
    container_name: xcplus3-gogs
    restart: unless-stopped
    ports:
      - "3000:3000"
      - "10022:22"
    volumes:
      - /data/docker/gogs:/data
    networks:
      - xcplus3-net

networks:
  xcplus3-net:
    driver: bridge
```

### 3.3 初始化 Nacos 与 XXL-Job 数据库

先启动 MySQL，再执行建库脚本：

```bash
# 启动 MySQL
docker-compose up -d mysql
sleep 30

# 创建 nacos 与 xxl_job 数据库（需提前准备 SQL 或手动执行）
docker exec -i xcplus3-mysql mysql -uroot -p${MYSQL_ROOT_PASSWORD} -e "
CREATE DATABASE IF NOT EXISTS nacos_config CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS xxl_job CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
"
```

导入 Nacos 与 XXL-Job 的初始化 SQL（可从二代 `initSql` 目录获取并适配）。

### 3.4 启动全部服务

```bash
docker-compose up -d
docker-compose ps
```

---

## 四、分步安装（可选）

若不用 Compose，可逐容器启动：

### 4.1 MySQL

```bash
docker run -d --name xcplus3-mysql \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=your_password \
  -v /data/docker/mysql:/var/lib/mysql \
  mysql:8.0 \
  --default-authentication-plugin=mysql_native_password --character-set-server=utf8mb4
```

### 4.2 Redis

```bash
docker run -d --name xcplus3-redis \
  -p 6379:6379 \
  -v /data/docker/redis:/data \
  redis:7.2-alpine redis-server --appendonly yes
```

### 4.3 Nacos

```bash
# 先建库 nacos_config，再启动
docker run -d --name xcplus3-nacos \
  -p 8848:8848 -p 9848:9848 \
  -e MODE=standalone \
  -e SPRING_DATASOURCE_PLATFORM=mysql \
  -e MYSQL_SERVICE_HOST=宿主机IP \
  -e MYSQL_SERVICE_DB_NAME=nacos_config \
  -e MYSQL_SERVICE_USER=root \
  -e MYSQL_SERVICE_PASSWORD=your_password \
  nacos/nacos-server:v2.4.0
```

### 4.4 RabbitMQ

```bash
docker run -d --name xcplus3-rabbitmq \
  -p 5672:5672 -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=admin \
  -e RABBITMQ_DEFAULT_PASS=admin123 \
  -v /data/docker/rabbitmq:/var/lib/rabbitmq \
  rabbitmq:3.13-management-alpine
```

### 4.5 MinIO

```bash
docker run -d --name xcplus3-minio \
  -p 9000:9000 -p 9001:9001 \
  -e MINIO_ROOT_USER=minioadmin \
  -e MINIO_ROOT_PASSWORD=minioadmin123 \
  -v /data/docker/minio:/data \
  minio/minio:RELEASE.2024-01-16T16-07-38Z server /data --console-address ":9001"
```

### 4.6 Elasticsearch

```bash
docker run -d --name xcplus3-elasticsearch \
  -p 9200:9200 -p 9300:9300 \
  -e discovery.type=single-node \
  -e xpack.security.enabled=false \
  -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
  -v /data/docker/elasticsearch:/usr/share/elasticsearch/data \
  elasticsearch:8.11.0
```

---

## 五、端口与访问

| 服务 | 端口 | 访问地址 |
|------|------|----------|
| MySQL | 3306 | `mysql -h 服务器IP -P 3306 -u root -p` |
| Redis | 6379 | `redis-cli -h 服务器IP -p 6379` |
| Nacos | 8848 | http://服务器IP:8848/nacos |
| RabbitMQ 管理 | 15672 | http://服务器IP:15672 |
| MinIO 控制台 | 9001 | http://服务器IP:9001 |
| Elasticsearch | 9200 | http://服务器IP:9200 |
| XXL-Job | 18088 | http://服务器IP:18088/xxl-job-admin |
| Gogs | 3000 | http://服务器IP:3000 |

---

## 六、安全建议

1. 修改默认密码：MySQL、Redis、RabbitMQ、MinIO、Nacos
2. 腾讯云安全组：仅开放必要端口，限制来源 IP
3. 生产环境启用 Elasticsearch 安全认证
4. 敏感配置使用环境变量或密钥管理

---

## 七、与二代差异说明

- **Nacos**：1.4.1 → 2.4，需使用 2.x 客户端与配置格式
- **XXL-Job**：2.3.1 → 2.4.2，兼容 Spring Boot 3
- **Elasticsearch**：7.12 → 8.11，API 有变更，需适配
- **Redis / RabbitMQ / MinIO**：版本升级，一般向后兼容
