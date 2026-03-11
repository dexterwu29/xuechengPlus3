# Nacos 鉴权配置说明

> 参考：[权限校验](https://nacos.io/docs/next/manual/admin/auth/) | [配置鉴权](https://nacos.io/docs/next/manual/user/auth/)

---

## 一、服务端（Docker Nacos）

### 1.1 环境变量对照

| 配置项 | Docker 环境变量 | 说明 |
|--------|-----------------|------|
| JWT 密钥 | **NACOS_AUTH_TOKEN** | Base64 编码，原始密钥 ≥32 字符 |
| 鉴权开关 | NACOS_AUTH_ENABLE | true 开启 |
| 身份 Key（集群） | NACOS_AUTH_IDENTITY_KEY | 用于集群节点间识别 |
| 身份 Value（集群） | NACOS_AUTH_IDENTITY_VALUE | 用于集群节点间识别 |

**重要**：Docker 环境变量为 `NACOS_AUTH_TOKEN`，不是 `NACOS_AUTH_TOKEN_SECRET_KEY`。

### 1.2 生成 Base64 密钥

```bash
# 必须用 -base64，不要用 -hex
openssl rand -base64 32
```

输出示例：`K7gNU3sdo+OL0wNhqoVWhr3g6s1xYv72ol/pe/Unols=`

### 1.3 单机模式启动（开启鉴权）

```bash
docker stop xcplus3-nacos 2>/dev/null; docker rm xcplus3-nacos 2>/dev/null

KEY=$(openssl rand -base64 32)
echo "生成的密钥: $KEY"

docker run -d \
  -e MODE=standalone \
  -e NACOS_AUTH_ENABLE=true \
  -e "NACOS_AUTH_TOKEN=$KEY" \
  -e NACOS_AUTH_IDENTITY_KEY=serverIdentity \
  -e NACOS_AUTH_IDENTITY_VALUE=security \
  -p 8848:8848 \
  -p 9848:9848 \
  -p 9849:9849 \
  --name xcplus3-nacos \
  --restart unless-stopped \
  nacos/nacos-server:v2.4.0
```

### 1.4 首次访问：初始化管理员密码

Nacos 2.4.0 已移除默认密码，开启鉴权后首次访问会跳转到**初始化页面**，需创建管理员 `nacos` 的密码。

- **方式一**：访问 http://服务器IP:8848/nacos ，在页面中设置密码
- **方式二**：API 初始化（需先等 Nacos 启动完成）：
  ```bash
  curl -X POST 'http://111.229.46.188:8848/nacos/v1/auth/users/admin' -d 'password=你的密码'
  ```

---

## 二、客户端（SDK / OpenAPI / Java 应用）

### 2.1 Spring Cloud 配置

在 `application.yml` 或 `application-local.yml` 中：

```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: 111.229.46.188:8848
        username: nacos
        password: 你设置的Nacos密码
      config:
        server-addr: 111.229.46.188:8848
        username: nacos
        password: 你设置的Nacos密码
```

### 2.2 OpenAPI 鉴权

1. 先登录获取 accessToken：
   ```bash
   curl -X POST 'http://111.229.46.188:8848/nacos/v3/auth/user/login' \
     -d 'username=nacos&password=你的密码'
   ```
   返回示例：`{"accessToken":"eyJhbGci...","tokenTtl":18000,"globalAdmin":true}`

2. 后续请求在 URL 后附加 `accessToken=xxx`：
   ```bash
   curl -X GET 'http://111.229.46.188:8848/nacos/v2/cs/config?accessToken=eyJhbGci...&dataId=nacos-demo.yaml&group=DEFAULT_GROUP'
   ```

### 2.3 nacos-demo 项目配置

复制 `application-example.yml` 为 `application-local.yml`，填入：

```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: 111.229.46.188:8848
        username: nacos
        password: 你设置的Nacos密码
      config:
        server-addr: 111.229.46.188:8848
        username: nacos
        password: 你设置的Nacos密码
```

---

## 三、常见错误

| 错误 | 原因 | 处理 |
|------|------|------|
| `the length of secret key must great than or equal 32 bytes` | 使用 hex 或错误 env 变量 | 用 `openssl rand -base64 32`，且 env 为 `NACOS_AUTH_TOKEN` |
| `The specified key byte array is 0 bits` | 密钥未正确传入 | 检查 env 变量名、引号、shell 转义 |
| 502 Bad Gateway | Nacos 未启动完成 | 等待 30～60 秒 |
