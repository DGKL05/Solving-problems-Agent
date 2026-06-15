# AI 智能刷题与错题管理系统

基于 Spring Boot 的 Web 课程设计项目。

本项目面向学习刷题场景，支持用户登录、图片识题、AI 解题、用户管理、题目管理、标签管理、错题管理、知识点管理、学习计划管理、公告管理、会话记录管理和 Redis 会话缓存等功能。

## 项目定位

本仓库已整理为适合《软件框架技术》课程设计提交的 Spring Boot Web 项目。系统不再保留微信小程序端，当前以前后端 Web 演示和 Spring Boot 后端接口为主。

核心流程：

```text
用户登录 -> 题目录入/图片识题 -> AI 解题 -> 错题收藏 -> 知识点沉淀 -> 学习计划 -> CRUD 管理
```

## 前端页面

启动项目后可以访问以下页面：

```text
http://localhost:8080/                 AI 解题页面
http://localhost:8080/admin.html       综合管理后台，推荐答辩演示使用
http://localhost:8080/crud.html        基础 CRUD 演示页
http://localhost:8080/extended-crud.html 扩展模块 CRUD 演示页
```

其中 `admin.html` 已整合以下模块，并支持新增、查询、编辑、删除：

```text
用户管理
题目管理
标签管理
错题管理
知识点管理
学习计划管理
公告管理
会话记录管理
```

## 课程设计匹配点

| 课程要求 | 项目实现 |
|---|---|
| Spring Boot 应用 | Spring Boot 3.3.0 |
| 数据持久化 | Spring Data JPA + MySQL |
| 前端展示 | static/index.html、static/admin.html、static/crud.html、static/extended-crud.html |
| 用户认证 | 注册、登录、游客登录、JWT 鉴权 |
| 缓存管理 | Redis 会话缓存 |
| 数据库脚本 | sql/agent_dome.sql |
| CRUD 模块 | 用户、题目、标签、错题、会话、知识点、学习计划、公告 |

## 核心模块

### 用户管理

- 分页查询用户
- 查询当前用户
- 新增用户
- 查询用户详情
- 修改用户信息
- 删除用户

### 题目管理

- 分页查询题目
- 按题型筛选题目
- 新增题目
- 查询题目详情
- 修改题目内容和答案
- 删除题目

### 标签管理

- 查询标签列表
- 新增标签
- 查询标签详情
- 修改标签名称和颜色
- 删除标签

### 错题管理

- 查询错题列表
- 分页查询错题
- 新增错题
- 查询错题详情
- 修改错题备注和复习次数
- 删除错题
- 清空错题

### 知识点管理

- 分页查询知识点
- 按科目筛选知识点
- 新增知识点
- 查询知识点详情
- 修改知识点分类、说明和掌握程度
- 删除知识点

### 学习计划管理

- 分页查询学习计划
- 按状态筛选学习计划
- 新增学习计划
- 查询学习计划详情
- 修改计划内容、目标数量和完成进度
- 删除学习计划

### 公告管理

- 分页查询公告
- 按状态或类型筛选公告
- 新增公告
- 查询公告详情
- 修改公告标题、内容和状态
- 删除公告

### 会话记录管理

- 新增会话
- 查询会话列表
- 查询会话详情
- 修改会话标题
- 删除会话
- 清空会话

### AI 解题与图片识题

- 图片上传
- OCR 识别题目文本
- AI 生成解题思路
- WebSocket 流式返回

## 技术栈

| 技术 | 作用 |
|---|---|
| Java 17 | 后端开发语言 |
| Spring Boot 3.3.0 | 应用框架 |
| Spring MVC | REST 接口 |
| Spring Data JPA | 数据访问 |
| MySQL 8 | 业务数据存储 |
| Redis 7 | 会话缓存 |
| MongoDB 7 | 聊天记录和图片数据 |
| JWT | 登录鉴权 |
| WebSocket | 流式解题输出 |
| Maven 多模块 | 工程组织 |

## 项目结构

```text
Solving-problems-Agent
├── common                         # 公共实体、Repository、异常、JWT 工具
├── user-service                   # 用户业务服务
├── image-pipeline                 # 图片上传、OCR、文本清洗
├── mistake-service                # 错题与标签服务
├── agent-core                     # Agent 编排、模型调用、会话记忆
├── gateway                        # Controller、WebSocket、静态页面
│   └── src/main/resources/static
│       ├── index.html             # AI 解题页面
│       ├── admin.html             # 综合管理后台
│       ├── crud.html              # 基础 CRUD 演示页
│       └── extended-crud.html     # 扩展 CRUD 演示页
├── docs
│   ├── CRUD_API.md                # CRUD 接口说明
│   └── EXTENDED_MODULES.md        # 扩展模块说明
├── sql
│   └── agent_dome.sql             # MySQL 初始化脚本
├── docker-compose.yml
└── pom.xml
```

## 本地启动

```bash
docker compose up -d
mvn clean package -DskipTests
mvn -pl gateway spring-boot:run
```

访问页面：

```text
http://localhost:8080/
http://localhost:8080/admin.html
http://localhost:8080/crud.html
http://localhost:8080/extended-crud.html
```

## 主要接口

详细接口见：

```text
docs/CRUD_API.md
docs/EXTENDED_MODULES.md
```

## 课程演示建议

```text
1. 启动 MySQL、Redis、MongoDB
2. 启动 gateway 模块
3. 访问 /admin.html
4. 注册或游客登录获取 Token
5. 演示用户、题目、标签、错题、知识点、学习计划、公告、会话记录的新增、查询、编辑、删除
6. 展示 sql/agent_dome.sql 数据库脚本
7. 展示 Redis 缓存和 MongoDB 会话记录
```

## 项目亮点

- 选题区别于图书、学生、教师等常见管理系统。
- Spring Boot 多模块结构，分层清晰。
- 已补充多组 CRUD 接口和对应前端页面。
- 新增知识点、学习计划、公告等独立业务模块，系统更完整。
- 覆盖安全、数据、前端、缓存四类核心技术。
