# 扩展业务模块说明

本次在原有用户、题目、标签、错题、会话记录模块之外，新增了 3 个独立业务模块，用于增强课程设计的管理系统完整度。

## 1. 知识点管理模块

表名：`knowledge_points`

接口：

```http
GET    /api/knowledge-points?page=1&size=10
GET    /api/knowledge-points?page=1&size=10&subjectType=ACM
POST   /api/knowledge-points
GET    /api/knowledge-points/{id}
PUT    /api/knowledge-points/{id}
DELETE /api/knowledge-points/{id}
```

示例 JSON：

```json
{
  "name": "动态规划",
  "subjectType": "ACM",
  "category": "算法",
  "description": "适用于最优子结构和重叠子问题。",
  "masteryLevel": 2
}
```

## 2. 学习计划管理模块

表名：`study_plans`

接口：

```http
GET    /api/study-plans?page=1&size=10
GET    /api/study-plans?page=1&size=10&status=TODO
POST   /api/study-plans
GET    /api/study-plans/{id}
PUT    /api/study-plans/{id}
DELETE /api/study-plans/{id}
```

示例 JSON：

```json
{
  "title": "ACM 动态规划专题复习",
  "subjectType": "ACM",
  "content": "完成基础 DP、背包、区间 DP 练习。",
  "targetCount": 20,
  "finishedCount": 0,
  "status": "TODO"
}
```

## 3. 公告管理模块

表名：`notices`

接口：

```http
GET    /api/notices?page=1&size=10
GET    /api/notices?page=1&size=10&status=PUBLISHED
GET    /api/notices?page=1&size=10&type=SYSTEM
POST   /api/notices
GET    /api/notices/{id}
PUT    /api/notices/{id}
DELETE /api/notices/{id}
```

示例 JSON：

```json
{
  "title": "系统使用说明",
  "content": "本系统支持 AI 解题、错题管理、知识点管理和学习计划管理。",
  "type": "SYSTEM",
  "status": "PUBLISHED"
}
```

## 4. 课程设计报告可写的新增模块

现在系统可写的功能模块包括：

```text
用户管理
题目管理
标签管理
错题管理
会话记录管理
知识点管理
学习计划管理
公告管理
AI 解题与图片识题
```
