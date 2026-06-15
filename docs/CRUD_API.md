# CRUD 接口说明

本文档用于《软件框架技术》课程设计答辩和接口测试说明。

## 认证方式

除 `/api/auth/**` 和 `/api/health` 外，其他接口都需要在请求头中携带 JWT：

```http
Authorization: Bearer <token>
```

## 1. 用户管理 CRUD

| 功能 | 方法 | 路径 | 说明 |
|---|---|---|---|
| 分页查询用户 | GET | `/api/users?page=1&size=10` | 查询用户列表 |
| 查询当前用户 | GET | `/api/users/me` | 查询当前登录用户 |
| 新增用户 | POST | `/api/users` | 新增 Web 用户 |
| 查询用户详情 | GET | `/api/users/{id}` | 根据 ID 查询用户 |
| 修改用户 | PUT | `/api/users/{id}` | 修改昵称、头像、角色、密码 |
| 删除用户 | DELETE | `/api/users/{id}` | 删除指定用户 |

新增用户示例：

```json
{
  "username": "test01",
  "password": "123456",
  "nickname": "测试用户",
  "role": "USER"
}
```

## 2. 题目管理 CRUD

| 功能 | 方法 | 路径 | 说明 |
|---|---|---|---|
| 分页查询题目 | GET | `/api/problems?page=1&size=10` | 查询当前用户题目 |
| 按类型查询题目 | GET | `/api/problems?page=1&size=10&subjectType=ACM` | 按 ACM/MATH/CS408 筛选 |
| 新增题目 | POST | `/api/problems` | 手动新增题目记录 |
| 查询题目详情 | GET | `/api/problems/{id}` | 查看题目详情 |
| 修改题目 | PUT | `/api/problems/{id}` | 修改题目文本、答案、错误类型 |
| 删除题目 | DELETE | `/api/problems/{id}` | 删除题目记录 |

新增题目示例：

```json
{
  "subjectType": "ACM",
  "cleanedText": "给定数组，求最大子段和。",
  "solutionText": "使用动态规划，dp[i] 表示以 i 结尾的最大子段和。",
  "solutionCode": "int ans = nums[0];"
}
```

## 3. 标签管理 CRUD

| 功能 | 方法 | 路径 | 说明 |
|---|---|---|---|
| 查询标签列表 | GET | `/api/tags` | 查询当前用户标签 |
| 新增标签 | POST | `/api/tags` | 创建标签 |
| 查询标签详情 | GET | `/api/tags/{id}` | 查询指定标签 |
| 修改标签 | PUT | `/api/tags/{id}` | 修改标签名称、颜色 |
| 删除标签 | DELETE | `/api/tags/{id}` | 删除标签 |

新增标签示例：

```json
{
  "name": "动态规划",
  "color": "#4A90D9"
}
```

## 4. 错题管理 CRUD

| 功能 | 方法 | 路径 | 说明 |
|---|---|---|---|
| 查询错题列表 | GET | `/api/mistakes` | 查询当前用户全部错题 |
| 分页查询错题 | GET | `/api/mistakes/page?page=1&size=10` | 分页查询错题收藏 |
| 新增错题 | POST | `/api/mistakes` | 把题目加入错题集 |
| 查询错题详情 | GET | `/api/mistakes/{id}` | 查询错题收藏详情 |
| 修改错题 | PUT | `/api/mistakes/{id}` | 修改备注、复习次数 |
| 删除错题 | DELETE | `/api/mistakes/{id}` | 删除单条错题 |
| 清空错题 | DELETE | `/api/mistakes` | 清空当前用户错题 |

新增错题示例：

```json
{
  "problemId": 1,
  "memo": "动态规划状态转移没有想清楚",
  "sessionId": "可选会话ID"
}
```

修改错题示例：

```json
{
  "memo": "已复习，注意边界条件",
  "reviewed": true
}
```

## 5. 会话记录 CRUD

| 功能 | 方法 | 路径 | 说明 |
|---|---|---|---|
| 查询会话列表 | GET | `/api/chat/sessions` | 查询当前用户会话 |
| 新增会话 | POST | `/api/chat/sessions` | 创建新会话 |
| 查询会话详情 | GET | `/api/chat/sessions/{sessionId}` | 查询聊天记录详情 |
| 修改会话标题 | PUT | `/api/chat/sessions/{sessionId}` | 修改会话标题 |
| 删除会话 | DELETE | `/api/chat/sessions/{sessionId}` | 删除指定会话 |
| 清空会话 | DELETE | `/api/chat/sessions` | 清空当前用户全部会话 |

修改会话示例：

```json
{
  "title": "动态规划专题练习"
}
```

## 6. 前端演示页面

启动项目后访问：

```text
http://localhost:8080/crud.html
```

该页面可以直接演示：用户、题目、标签、错题、会话记录等管理功能。
