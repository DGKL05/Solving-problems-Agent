<div align="center">

# AI 智能刷题与错题管理系统

### 基于 Spring Boot 的 Web 课程设计项目

面向学习场景的 AI 解题系统，支持 **用户登录、图片识题、AI 解题、错题管理、标签管理、会话记录、Redis 会话缓存** 等功能。

</div>

---

## 项目简介

本项目原名 `Solving-problems-Agent / AgentDome`，现整理为适合《软件框架技术》课程设计提交的 Spring Boot Web 项目。

系统围绕“刷题学习”场景设计，用户可以通过 Web 页面登录系统，上传题目图片或输入题目内容，系统调用 OCR 与大模型能力进行题目解析和智能解答，并支持将题目加入错题集、维护错题标签、保存历史会话，形成完整的学习闭环。

核心流程：

```text
用户登录 -> 上传题目/输入题目 -> OCR 识别 -> AI 解题 -> 错题收藏 -> 标签管理 -> 会话沉淀
```

> 本仓库已移除微信小程序端，当前保留 Spring Boot 后端与 Web 静态页面，便于课程设计演示和提交。

---

## 课程设计匹配点

| 课程要求 | 项目对应实现 |
|---|---|
| Spring Boot 应用系统 | 使用 Spring Boot 3.3.0 构建后端服务 |
| 数据持久化 | 使用 Spring Data JPA + MySQL 管理用户、题目、错题、标签数据 |
| 前端页面展示 | 使用 `gateway/src/main/resources/static/index.html` 作为 Web 演示页面 |
| 用户认证与安全 | 支持 Web 注册、登录、游客登录、JWT Token 鉴权、接口拦截 |
| 缓存管理 | 使用 Redis 缓存会话上下文数据，设置 24 小时过期时间 |
| 功能模块 | 用户认证、AI 解题、图片上传、错题管理、标签管理、聊天记录管理 |
| 数据库脚本 | `sql/agent_dome.sql` 提供 MySQL 初始化脚本 |
| 本地部署 | `docker-compose.yml` 一键启动 MySQL、Redis、MongoDB |

---

## 核心功能

### 1. 用户认证模块

- Web 用户注册
- Web 用户登录
- 游客登录
- JWT Token 生成与校验
- 未登录接口拦截
- 登录频率限制，防止频繁请求

### 2. AI 解题模块

- 支持文本题目解答
- 支持 WebSocket 流式输出
- 支持 ACM、数学、408 等题型扩展
- 支持调用 Qwen / DashScope 大模型
- 支持在无模型配置时保留基础系统流程演示

### 3. 图片识题模块

- 支持 Multipart 图片上传
- 支持 OCR 识别题目文本
- 支持清洗 OCR 噪声文本
- 图片信息可存储到 MongoDB / GridFS

### 4. 错题管理模块

- 添加错题
- 查询错题列表
- 查看错题内容
- 删除错题
- 清空错题
- 记录错题备注、错误类型和复习次数

### 5. 标签管理模块

- 创建标签
- 查询用户标签
- 删除标签
- 题目与标签关联
- 支持按照标签扩展错题筛选

### 6. 会话记录模块

- 创建新会话
- 保存用户与 AI 的对话记录
- 查询会话列表
- 删除指定会话
- 清空全部会话
- Redis 保存短期上下文，MongoDB 保存长期聊天记录

---

## 技术栈

### 后端技术

| 技术 | 说明 |
|---|---|
| Java 17 | 后端开发语言 |
| Spring Boot 3.3.0 | 项目基础框架 |
| Spring MVC | REST 接口开发 |
| Spring Data JPA | MySQL 数据访问 |
| MySQL 8 | 结构化业务数据存储 |
| Redis 7 | 会话上下文缓存 |
| MongoDB 7 | 图片、聊天记录、历史摘要存储 |
| JWT | 登录认证与接口鉴权 |
| WebSocket | AI 解题流式输出 |
| Maven Multi-Module | 多模块工程管理 |
| LangChain4j / DashScope | 大模型能力集成 |

### 前端技术

| 技术 | 说明 |
|---|---|
| HTML / CSS / JavaScript | Web 演示页面 |
| Fetch API | 调用后端 REST 接口 |
| WebSocket | 接收 AI 流式输出 |

---

## 项目结构

```text
Solving-problems-Agent
├── common                         # 公共实体、Repository、异常、JWT 工具、配置
│   └── src/main/java/com/agentdome/common
├── user-service                   # 用户登录、微信认证、用户业务服务
├── image-pipeline                 # 图片上传、OCR 识别、文本清洗
├── mistake-service                # 错题集、标签、错题查询与删除
├── agent-core                     # Agent 编排、Qwen 调用、工具路由、会话记忆
├── gateway                        # Web 入口、Controller、WebSocket、静态页面
│   └── src/main/resources/static/index.html
├── sql                            # MySQL 初始化脚本
│   └── agent_dome.sql
├── docker-compose.yml             # MySQL、Redis、MongoDB 本地环境
└── pom.xml                        # Maven 父工程
```

---

## 数据库设计

项目主要包含以下 MySQL 表：

| 表名 | 说明 |
|---|---|
| users | 用户信息表 |
| problems | 题目记录表 |
| mistake_collections | 错题收藏表 |
| tags | 标签表 |
| problem_tags | 题目标签关联表 |

数据库初始化脚本位置：

```text
sql/agent_dome.sql
```

---

## 本地启动

### 1. 克隆项目

```bash
git clone https://github.com/DGKL05/Solving-problems-Agent.git
cd Solving-problems-Agent
```

### 2. 启动基础环境

```bash
docker compose up -d
```

默认启动服务：

| 服务 | 端口 | 默认信息 |
|---|---:|---|
| MySQL | 3306 | 数据库：`agent_dome`，root 密码：`root` |
| Redis | 6379 | 密码：`redis123` |
| MongoDB | 27017 | 用户：`admin`，密码：`admin123` |

### 3. 配置可选环境变量

Windows PowerShell：

```powershell
$env:MYSQL_HOST="localhost"
$env:MYSQL_USER="root"
$env:MYSQL_PASSWORD="root"
$env:REDIS_PASSWORD="redis123"
$env:MONGO_ROOT_USER="admin"
$env:MONGO_ROOT_PASSWORD="admin123"
$env:DASHSCOPE_API_KEY="你的 DashScope API Key"
$env:JWT_SECRET="please-change-this-secret-to-a-long-random-string-32bit"
```

没有配置 `DASHSCOPE_API_KEY` 时，部分 AI 调用能力可能不可用，但基础项目结构、登录、数据库和页面仍可用于课程设计展示。

### 4. 编译项目

```bash
mvn clean package -DskipTests
```

### 5. 启动后端

```bash
mvn -pl gateway spring-boot:run
```

### 6. 访问 Web 页面

```text
http://localhost:8080/
```

健康检查接口：

```text
GET http://localhost:8080/api/health
```

正常返回：

```json
{
  "code": 0,
  "data": "AgentDome running",
  "message": "success"
}
```

---

## 主要接口

### 用户认证

```http
POST /api/auth/register
POST /api/auth/web-login
POST /api/auth/guest
```

### 图片上传

```http
POST /api/chat/upload
Content-Type: multipart/form-data
```

### WebSocket 解题

```text
ws://localhost:8080/ws/chat?token=你的JWT
```

### 错题管理

```http
GET    /api/mistakes
DELETE /api/mistakes/{id}
```

### 系统健康检查

```http
GET /api/health
```

---

## 课程演示建议

建议答辩演示顺序：

```text
1. docker compose up -d 启动 MySQL、Redis、MongoDB
2. 启动 gateway 模块
3. 访问 http://localhost:8080/
4. 注册或游客登录，获取 JWT
5. 上传题目图片或输入题目文本
6. 展示 AI 解题返回
7. 将题目加入错题集
8. 查询错题记录
9. 展示 Redis、MySQL、MongoDB 在系统中的作用
```

---

## 项目亮点

- 选题区别于图书、学生、教师等常见管理系统，原创性更强。
- 采用 Spring Boot 多模块结构，分层清晰，职责明确。
- 覆盖课程要求的安全、数据、前端、缓存四类核心技术。
- 使用 MySQL 管理结构化数据，Redis 管理短期会话缓存，MongoDB 管理聊天记录和图片数据。
- 结合 AI 解题、OCR 识别和错题沉淀，体现一定创新性。

---

## 后续可优化方向

- 补充错题分页查询接口
- 补充标签修改接口
- 增加管理员角色和后台管理页面
- 增加 Swagger / Knife4j 接口文档
- 增加单元测试和接口测试用例
- 增加 AI Mock 模式，提升无网络环境下的演示稳定性
