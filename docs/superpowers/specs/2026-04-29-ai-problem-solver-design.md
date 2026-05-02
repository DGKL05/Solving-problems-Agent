# Agent-First AI Problem Solver — 设计规格说明书

> 版本: v1.0
> 日期: 2026-04-29
> 状态: Draft

---

## 1. 项目概述

一个基于微信小程序的 AI 自动解题工具。用户拍照或从图库上传题目图片，系统通过 OCR + 大模型进行自动解题，并通过 LangChain Agent 提供对话式交互体验（如"加入错题集"、"复习"等命令式操作）。

### 1.1 目标用户

- ACM 算法竞赛选手
- 大学生（高等数学、考研408考生）
- 需要刷题辅导的学生群体

### 1.2 核心差异化

Agent-first 交互模式。不是简单的"拍照出答案"，而是一个有记忆、能理解意图、能管理学习流程的对话式 AI 助教。

---

## 2. 技术栈

| 层 | 技术 |
|---|---|
| 前端 | 微信小程序 |
| 后端框架 | Spring Boot (Maven 多模块) |
| 结构化存储 | MySQL |
| 缓存/会话 | Redis |
| 非结构化存储 | MongoDB (图片 + 聊天记录) |
| AI 框架 | LangChain4j |
| 大模型 | 阿里云 Qwen-Max / Qwen-Plus |
| OCR | 阿里云 OCR API |
| 认证 | 微信 OpenID |
| 部署 | 自建服务器 (阿里云) |

---

## 3. 系统架构

### 3.1 模块划分

Maven 父工程 `agent-dome`，包含 5 个子模块：

| 模块 | 职责 |
|---|---|
| `gateway` | 统一 REST API + WebSocket 入口 |
| `agent-core` | LangChain 工具定义 + Function Calling 编排 + 对话管理 |
| `image-pipeline` | 图片上传 → 阿里云 OCR → 文本清洗 |
| `mistake-service` | 错题集 CRUD + 标签管理 + 推荐查询 |
| `user-service` | 微信 OpenID 认证 + 用户资料管理 |

### 3.2 数据层分配

- **MySQL**: users, problems, mistake_collections, tags, problem_tags
- **Redis**: 活跃会话记忆 (TTL 自动过期)、对话上下文、限流
- **MongoDB**: 原始图片 (GridFS)、OCR 原始结果、历史聊天记录、跨会话摘要

### 3.3 核心流程

**拍照解题流程:**
小程序拍照 → 上传到后端 → MongoDB GridFS 存储 → 阿里云 OCR → 文本清洗 → agent-core 根据 subjectType 调用对应 Qwen-Max prompt → 返回结果到小程序

**加入错题集流程:**
用户输入"加入错题集" → LangChain function calling 识别意图 → 触发 `add_to_mistakes` 工具 → mistake-service 写入 MySQL

**跨会话记忆流程:**
当前会话结束 → agent-core 生成结构性摘要（错题记录、薄弱知识点） → 存入 MongoDB → 下次会话自动加载摘要到 Redis

---

## 4. Agent 设计

### 4.1 工具定义

| 工具 | 参数 | 说明 |
|---|---|---|
| `solve_problem` | imageUrl, subjectType | OCR + Qwen-Max 解题 |
| `add_to_mistakes` | problemId, tags[], errorType, notes | 加入错题集 |
| `query_mistakes` | tag, dateRange, page, limit | 查询/复习错题 |
| `recommend_similar` | problemId, count | 基于标签推荐同类题目 |
| `explain_concept` | concept, subjectType | 概念讲解 |

### 4.2 调用方式

使用 Qwen 原生 Function Calling 能力。LangChain 提供工具抽象层和对话管理，不引入完整的 ReAct Agent 循环（避免不必要的中间推理 token 消耗）。

### 4.3 Prompt 策略

三层结构:

1. **System Prompt** — Agent 身份设定、可用工具描述、对话格式指令
2. **Subject Prompt** — 按 subjectType 切换：
   - ACM: 代码生成 + 复杂度分析导向
   - Math: 分步推导 + 公式解释
   - 408: 概念解析 + 计算过程 + 图表说明
3. **Memory Context** — 当前会话记忆 (Redis) + 跨会话摘要 (MongoDB)

---

## 5. 数据模型

### 5.1 MySQL 表结构

**users**
```sql
id         BIGINT PK AUTO_INCREMENT
openid     VARCHAR(100) UNIQUE NOT NULL   -- 微信 OpenID
nickname   VARCHAR(100)
avatar_url VARCHAR(255)
created_at       DATETIME DEFAULT NOW()
last_active_at   DATETIME
```

**problems**
```sql
id                BIGINT PK AUTO_INCREMENT
user_id           BIGINT FK → users
subject_type      ENUM('ACM','MATH','CS408')
ocr_raw_text      TEXT                    -- OCR 原始输出
cleaned_text      TEXT                    -- 清洗后题目文本
original_image_id VARCHAR(100)            -- MongoDB GridFS ID
solution_text     TEXT                    -- AI 解答
solution_code     TEXT                    -- AI 生成代码 (仅 ACM)
tags              JSON                    -- 用户标注标签
error_type        VARCHAR(50)             -- 错因分类
created_at        DATETIME DEFAULT NOW()
```

**mistake_collections**
```sql
id            BIGINT PK AUTO_INCREMENT
user_id       BIGINT FK → users
problem_id    BIGINT FK → problems
session_id    VARCHAR(100)                -- 来源会话 ID
memo          TEXT                        -- 用户备注
review_count  INT DEFAULT 0
last_reviewed_at DATETIME
created_at    DATETIME DEFAULT NOW()
```

**tags**
```sql
id      BIGINT PK AUTO_INCREMENT
user_id BIGINT FK → users
name    VARCHAR(50) NOT NULL
color   VARCHAR(20)
UNIQUE(user_id, name)
```

**problem_tags** (多对多关联表)
```sql
problem_id BIGINT FK → problems
tag_id     BIGINT FK → tags
PRIMARY KEY(problem_id, tag_id)
```

### 5.2 MongoDB 文档结构

**chat_sessions**
```json
{
  "_id": "ObjectId",
  "session_id": "UUID",
  "user_id": 123,
  "messages": [
    {
      "role": "user|assistant|tool",
      "content": "...",
      "timestamp": "ISO8601"
    }
  ],
  "summary": {
    "problems_solved": 5,
    "weak_topics": ["DP", "cache"],
    "mistakes_added": 2
  },
  "created_at": "ISODate",
  "ended_at": "ISODate"
}
```

**images** (GridFS)
```json
{
  "_id": "ObjectId",
  "user_id": 123,
  "filename": "original_name.jpg",
  "ocr_text": "OCR 识别结果",
  "content_type": "image/jpeg",
  "upload_date": "ISODate"
}
```

---

## 6. 图片管道 (Image Pipeline)

### 6.1 处理流

1. 小程序 `wx.chooseMedia` 拍照/选图
2. 弹出 Subject Type 选择器（ACM 算法 / 高等数学 / 考研408）
3. 上传到 Spring Boot (`MultipartFile`)
4. 存储到 MongoDB GridFS
5. 调用阿里云 OCR API 识别文字
6. 文本清洗（去噪、合并段落、保留代码/公式格式）
7. 传入 agent-core 解题目
8. 结果通过 WebSocket 流式返回小程序

### 6.2 异常处理

- OCR 识别率低: 将原文图片 + 部分文本传给模型，让模型推断
- 图片模糊: 提示用户重拍
- 超时: 异步处理 + 轮询

---

## 7. 微信小程序设计

### 7.1 页面结构

| 页面 | 说明 |
|---|---|
| 会话列表 | 历史对话列表，入口按钮 |
| 聊天页 (核心) | 消息流 + 拍照/图库入口 + Agent 消息流式渲染 |
| 错题集 | 标签分组展示，可筛选/搜索 |
| 我的 | 用户信息、设置 |

### 7.2 底部 Tab

**聊天** | **错题集** | **我的**

### 7.3 关键交互

- 拍照/选图 → 弹出科目选择器（3个大按钮）→ 自动上传 → 消息展示在聊天流中
- 文字消息 → WebSocket → Agent 处理 → 流式响应
- Agent 在聊天中提供建议标签，用户可一键确认/修改
- 错题集页面支持按标签筛选、点击查看原题+解答

### 7.4 认证

使用 `wx.login` 获取 code → 后端换取 session_key → 生成自定义 token → 后续请求携带 token 识别用户身份

---

## 8. 部署架构

```
微信小程序 ←→ Spring Boot (阿里云 ECS)
                   ├── MySQL (阿里云 RDS)
                   ├── Redis (阿里云 Redis)
                   └── MongoDB (阿里云 MongoDB)
```

所有服务部署在同一阿里云 VPC 内，小程序通过 HTTPS + WSS 连接后端 API。

---

## 9. MVP 范围

### 包含

- [x] 微信小程序三页面（聊天、错题集、我的）
- [x] 拍照/选图上传 + 科目选择
- [x] 阿里云 OCR 文本识别
- [x] Qwen-Max 多科目解题（ACM / Math / 408）
- [x] Agent 5 个工具：solve, add_to_mistakes, query_mistakes, recommend_similar, explain_concept
- [x] 错题集 CRUD + 标签管理
- [x] 会话记忆（Redis 活跃会话 + MongoDB 摘要持久化）
- [x] 微信 OpenID 认证

### 不包含

- 在线代码执行沙箱
- 错题集间隔重复 / 复习提醒
- 知识图谱推荐
- 批量多图处理
- 离线模式
- 多语言支持

---

## 10. 设计原则

- **Agent-first**: 所有交互通过对话 Agent 编排，不为特定功能做独立页面
- **YAGNI**: 只实现 MVP 所需的最小功能集
- **隔离性**: 每个模块职责单一，通过接口通信，内部实现可独立变更
- **可追踪**: problems 表保存从 OCR 原始文本到最终解答的完整链路
