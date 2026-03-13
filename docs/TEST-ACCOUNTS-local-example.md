# 测试账号（本地用，复制为 TEST-ACCOUNTS-local.md 并加入 .gitignore）

密码均为：**password**

## SuperAdmin（系统管理员）

| 用户名 | 密码 | 说明 |
|--------|------|------|
| superadmin1 | password | 系统管理员1 |
| superadmin2 | password | 系统管理员2 |

## 机构1 教师（学成教育）

| 用户名 | 密码 | 说明 |
|--------|------|------|
| teacher1_org1 | password | 学成-张老师 |
| teacher2_org1 | password | 学成-李老师 |

## 机构2 教师（极客学院）

| 用户名 | 密码 | 说明 |
|--------|------|------|
| teacher1_org2 | password | 极客-王老师 |
| teacher2_org2 | password | 极客-刘老师 |

## 访客/学员

| 用户名 | 密码 | 说明 |
|--------|------|------|
| visitor1 | password | 学员小明 |
| visitor2 | password | 学员小红 |

## 登录示例

```bash
# 教师登录（机构1）
curl -X POST "http://localhost:11001/content/auth/login?username=teacher1_org1&password=password"

# 返回 token，后续请求在 Header 中携带：
# Authorization: Bearer <token>
# 教师可不传 X-Company-Id，系统从 JWT 解析机构
```

## 机构 ID

- 机构1（学成教育）：company_id = 1
- 机构2（极客学院）：company_id = 2
