-- AgentDome / AI 智能刷题与错题管理系统 MySQL 初始化脚本
-- 适用于《软件框架技术》课程设计提交与本地 Docker 初始化

CREATE DATABASE IF NOT EXISTS agent_dome
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE agent_dome;

DROP TABLE IF EXISTS problem_tags;
DROP TABLE IF EXISTS mistake_collections;
DROP TABLE IF EXISTS study_plans;
DROP TABLE IF EXISTS knowledge_points;
DROP TABLE IF EXISTS notices;
DROP TABLE IF EXISTS tags;
DROP TABLE IF EXISTS problems;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  openid VARCHAR(100) DEFAULT NULL COMMENT '微信或游客openid',
  username VARCHAR(50) DEFAULT NULL COMMENT 'Web登录用户名',
  password VARCHAR(255) DEFAULT NULL COMMENT 'BCrypt加密密码',
  nickname VARCHAR(100) DEFAULT NULL COMMENT '用户昵称',
  avatar_url VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
  role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色：ADMIN/USER',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  last_active_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '最后活跃时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_openid (openid),
  UNIQUE KEY uk_users_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE problems (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '题目ID',
  user_id BIGINT UNSIGNED NOT NULL COMMENT '所属用户ID',
  subject_type VARCHAR(10) NOT NULL COMMENT '题目类型：ACM/MATH/CS408',
  ocr_raw_text TEXT COMMENT 'OCR原始文本',
  cleaned_text TEXT COMMENT '清洗后的题目文本',
  original_image_id VARCHAR(100) DEFAULT NULL COMMENT 'MongoDB/GridFS图片ID',
  solution_text TEXT COMMENT 'AI解答内容',
  solution_code TEXT COMMENT '题目代码答案',
  error_type VARCHAR(50) DEFAULT NULL COMMENT '错误类型',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_problems_user_id (user_id),
  KEY idx_problems_subject_type (subject_type),
  KEY idx_problems_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目表';

CREATE TABLE tags (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  name VARCHAR(50) NOT NULL COMMENT '标签名称',
  color VARCHAR(20) DEFAULT '#999999' COMMENT '标签颜色',
  PRIMARY KEY (id),
  UNIQUE KEY uk_tags_user_name (user_id, name),
  KEY idx_tags_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

CREATE TABLE mistake_collections (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '错题收藏ID',
  user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  problem_id BIGINT UNSIGNED NOT NULL COMMENT '题目ID',
  session_id VARCHAR(100) DEFAULT NULL COMMENT '会话ID',
  memo TEXT COMMENT '错题备注',
  review_count INT DEFAULT 0 COMMENT '复习次数',
  last_reviewed_at DATETIME DEFAULT NULL COMMENT '最后复习时间',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_mistakes_user_id (user_id),
  KEY idx_mistakes_problem_id (problem_id),
  KEY idx_mistakes_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='错题收藏表';

CREATE TABLE problem_tags (
  problem_id BIGINT UNSIGNED NOT NULL COMMENT '题目ID',
  tag_id BIGINT UNSIGNED NOT NULL COMMENT '标签ID',
  PRIMARY KEY (problem_id, tag_id),
  KEY idx_problem_tags_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目标签关联表';

CREATE TABLE knowledge_points (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '知识点ID',
  user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  name VARCHAR(100) NOT NULL COMMENT '知识点名称',
  subject_type VARCHAR(20) DEFAULT NULL COMMENT '所属科目',
  category VARCHAR(50) DEFAULT NULL COMMENT '知识点分类',
  description TEXT COMMENT '知识点说明',
  mastery_level INT DEFAULT 0 COMMENT '掌握程度：0-5',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_kp_user_id (user_id),
  KEY idx_kp_subject_type (subject_type),
  KEY idx_kp_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识点表';

CREATE TABLE study_plans (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '学习计划ID',
  user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  title VARCHAR(100) NOT NULL COMMENT '计划标题',
  subject_type VARCHAR(20) DEFAULT NULL COMMENT '所属科目',
  content TEXT COMMENT '计划内容',
  target_count INT DEFAULT 0 COMMENT '目标完成数量',
  finished_count INT DEFAULT 0 COMMENT '已完成数量',
  status VARCHAR(20) DEFAULT 'TODO' COMMENT '状态：TODO/DOING/DONE',
  start_date DATE DEFAULT NULL COMMENT '开始日期',
  end_date DATE DEFAULT NULL COMMENT '结束日期',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_plan_user_id (user_id),
  KEY idx_plan_status (status),
  KEY idx_plan_subject_type (subject_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习计划表';

CREATE TABLE notices (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  title VARCHAR(120) NOT NULL COMMENT '公告标题',
  content TEXT COMMENT '公告内容',
  type VARCHAR(20) DEFAULT 'SYSTEM' COMMENT '公告类型',
  status VARCHAR(20) DEFAULT 'PUBLISHED' COMMENT '状态：DRAFT/PUBLISHED',
  created_by BIGINT UNSIGNED DEFAULT NULL COMMENT '发布人ID',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_notice_type (type),
  KEY idx_notice_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';

-- 演示账号：用户名 admin，密码建议启动后通过注册接口创建真实账号。
-- 当前项目使用 BCrypt 密码，直接 SQL 初始化明文密码无法登录。
INSERT INTO users (openid, username, password, nickname, role)
VALUES ('guest_demo_admin', 'admin', NULL, '管理员演示账号', 'ADMIN')
ON DUPLICATE KEY UPDATE nickname = VALUES(nickname), role = VALUES(role);

INSERT INTO tags (user_id, name, color)
VALUES
  (1, '算法', '#4A90D9'),
  (1, '数学', '#67C23A'),
  (1, '408', '#E6A23C')
ON DUPLICATE KEY UPDATE color = VALUES(color);

INSERT INTO knowledge_points (user_id, name, subject_type, category, description, mastery_level)
VALUES
  (1, '动态规划', 'ACM', '算法', '常见算法思想，适合用于最优子结构问题。', 2),
  (1, '二叉树遍历', 'CS408', '数据结构', '前序、中序、后序和层序遍历。', 3)
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO study_plans (user_id, title, subject_type, content, target_count, finished_count, status)
VALUES
  (1, 'ACM 动态规划专题复习', 'ACM', '完成基础 DP、背包、区间 DP 的复习。', 20, 0, 'TODO')
ON DUPLICATE KEY UPDATE title = VALUES(title);

INSERT INTO notices (title, content, type, status, created_by)
VALUES
  ('系统使用说明', '本系统用于 AI 解题、错题管理、知识点管理和学习计划管理。', 'SYSTEM', 'PUBLISHED', 1);
