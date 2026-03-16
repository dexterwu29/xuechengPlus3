/*
 * 学成在线三代 - 新同学版本初始化脚本
 * 版本：v3.0-new-student
 * 数据库：xcplus_content
 *
 * 核心变更：
 * 1. 课程状态使用三位编码：000草稿/100待审核/111已发布/112已下架
 * 2. 新增站内信表 system_message
 * 3. 新增审核日志表 course_audit_log
 * 4. 角色：visitor / teacher / super_admin
 */

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 创建数据库
-- ----------------------------
CREATE DATABASE IF NOT EXISTS `xcplus_content` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `xcplus_content`;

-- ----------------------------
-- 一、用户与权限模块
-- ----------------------------

-- 用户表
DROP TABLE IF EXISTS `xc_user`;
CREATE TABLE `xc_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(64) NOT NULL COMMENT '登录名',
  `password_hash` varchar(128) NOT NULL COMMENT 'BCrypt 密码哈希',
  `real_name` varchar(64) DEFAULT NULL COMMENT '真实姓名',
  `role` varchar(32) NOT NULL COMMENT 'super_admin/teacher/visitor',
  `company_id` bigint DEFAULT NULL COMMENT '机构ID，teacher 必填',
  `purchased_courses` json DEFAULT NULL COMMENT '已购买课程ID数组 ["1","5","10"]',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1启用 0禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_company_role` (`company_id`, `role`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 站内信表
DROP TABLE IF EXISTS `system_message`;
CREATE TABLE `system_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `type` varchar(32) NOT NULL COMMENT '消息类型：COURSE_SUBMIT/AUDIT_APPROVED/AUDIT_REJECTED/AUDIT_BANNED',
  `title` varchar(200) NOT NULL COMMENT '标题',
  `content` text NOT NULL COMMENT '内容',
  `course_id` bigint DEFAULT NULL COMMENT '关联课程ID',
  `course_name` varchar(200) DEFAULT NULL COMMENT '课程名称（冗余）',
  `from_user_id` bigint DEFAULT NULL COMMENT '发送者ID',
  `from_user_name` varchar(64) DEFAULT NULL COMMENT '发送者姓名（冗余）',
  `to_user_id` bigint DEFAULT NULL COMMENT '接收者ID（单发）',
  `to_role` varchar(32) DEFAULT NULL COMMENT '接收角色（广播：super_admin）',
  `is_read` tinyint NOT NULL DEFAULT 0 COMMENT '是否已读 0未读 1已读',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `read_time` datetime DEFAULT NULL COMMENT '阅读时间',
  PRIMARY KEY (`id`),
  KEY `idx_to_user` (`to_user_id`),
  KEY `idx_to_role` (`to_role`),
  KEY `idx_course_id` (`course_id`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站内信表';

-- ----------------------------
-- 二、课程管理模块
-- ----------------------------

-- 课程分类表
DROP TABLE IF EXISTS `course_category`;
CREATE TABLE `course_category` (
  `id` varchar(20) NOT NULL COMMENT '主键',
  `name` varchar(32) NOT NULL COMMENT '分类名称',
  `label` varchar(32) DEFAULT NULL COMMENT '分类标签',
  `parent_id` varchar(20) NOT NULL DEFAULT '0' COMMENT '父节点ID',
  `is_visible` tinyint NOT NULL DEFAULT 1 COMMENT '是否可见',
  `order_by` int NOT NULL DEFAULT 0 COMMENT '排序',
  `is_leaf` tinyint NOT NULL DEFAULT 1 COMMENT '是否为叶子节点',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` varchar(64) DEFAULT NULL,
  `update_by` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程分类';

-- 课程基本信息表（核心状态字段）
DROP TABLE IF EXISTS `course_base`;
CREATE TABLE `course_base` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `company_id` bigint NOT NULL COMMENT '机构ID',
  `company_name` varchar(255) DEFAULT NULL COMMENT '机构名称（冗余）',
  `name` varchar(100) NOT NULL COMMENT '课程名称',
  `users` varchar(500) DEFAULT NULL COMMENT '适用人群',
  `tags` varchar(50) DEFAULT NULL COMMENT '课程标签',
  `mt` varchar(20) DEFAULT NULL COMMENT '大分类ID',
  `st` varchar(20) DEFAULT NULL COMMENT '小分类ID',
  `grade` varchar(32) DEFAULT NULL COMMENT '课程等级',
  `teach_mode` varchar(32) DEFAULT NULL COMMENT '教育模式',
  `description` text COMMENT '课程介绍',
  `pic` varchar(500) DEFAULT NULL COMMENT '课程图片（旧字段，兼容）',
  `cover_pics` text DEFAULT NULL COMMENT '封面图fileId数组JSON，最多3个',
  `default_cover_index` tinyint NOT NULL DEFAULT 0 COMMENT '默认封面索引0-2',
  `course_status` varchar(10) NOT NULL DEFAULT '000' COMMENT '课程状态：000草稿/100待审核/111已发布/112已下架',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '是否已删除',
  PRIMARY KEY (`id`),
  KEY `idx_company_id` (`company_id`),
  KEY `idx_course_status` (`course_status`),
  KEY `idx_mt_st` (`mt`,`st`),
  KEY `idx_create_by` (`create_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程基本信息';

-- 课程营销信息表
DROP TABLE IF EXISTS `course_market`;
CREATE TABLE `course_market` (
  `id` bigint NOT NULL COMMENT '主键，课程ID',
  `charge` varchar(32) DEFAULT NULL COMMENT '收费规则：201000免费/201001收费',
  `price` float DEFAULT NULL COMMENT '现价',
  `original_price` float DEFAULT NULL COMMENT '原价',
  `qq` varchar(32) NOT NULL COMMENT '咨询QQ（必填）',
  `wechat` varchar(64) NOT NULL COMMENT '微信（必填）',
  `phone` varchar(32) NOT NULL COMMENT '电话（必填，1[3-9]开头）',
  `valid_days` int DEFAULT NULL COMMENT '有效期天数',
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` varchar(64) DEFAULT NULL,
  `update_by` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程营销信息';

-- 课程教师表
DROP TABLE IF EXISTS `course_teacher`;
CREATE TABLE `course_teacher` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `teacher_name` varchar(60) NOT NULL COMMENT '教师姓名',
  `position` varchar(255) DEFAULT NULL COMMENT '职位',
  `description` varchar(1024) DEFAULT NULL COMMENT '教师简介',
  `photograph` varchar(1024) DEFAULT NULL COMMENT '照片',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` varchar(64) DEFAULT NULL,
  `update_by` varchar(64) DEFAULT NULL,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_course_id` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程教师';

-- 课程计划表（章/节）
DROP TABLE IF EXISTS `teachplan`;
CREATE TABLE `teachplan` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `pname` varchar(64) NOT NULL COMMENT '计划名称',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父级ID',
  `grade` smallint NOT NULL DEFAULT 1 COMMENT '层级(1章/2节)',
  `media_type` varchar(10) DEFAULT NULL COMMENT '课程类型',
  `order_by` int NOT NULL DEFAULT 0 COMMENT '排序',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `status` int DEFAULT 1 COMMENT '状态',
  `is_preview_enabled` char(1) DEFAULT '0' COMMENT '是否可试看 0否 1是',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` varchar(64) DEFAULT NULL,
  `update_by` varchar(64) DEFAULT NULL,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_course_id` (`course_id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程计划';

-- ----------------------------
-- 三、审核模块
-- ----------------------------

-- 课程审核日志表
DROP TABLE IF EXISTS `course_audit_log`;
CREATE TABLE `course_audit_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `course_name` varchar(200) DEFAULT NULL COMMENT '课程名称（冗余）',
  `company_id` bigint NOT NULL COMMENT '机构ID',
  `company_name` varchar(200) DEFAULT NULL COMMENT '机构名称（冗余）',
  `audit_action` varchar(32) NOT NULL COMMENT '审核动作：submit/approve/reject/ban/offline',
  `audit_opinion` text COMMENT '审核意见/退回原因',
  `auditor_id` bigint DEFAULT NULL COMMENT '审核人ID',
  `auditor_name` varchar(64) DEFAULT NULL COMMENT '审核人姓名（冗余）',
  `old_status` varchar(10) DEFAULT NULL COMMENT '原状态 000/100/111/112',
  `new_status` varchar(10) DEFAULT NULL COMMENT '新状态 000/100/111/112',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_course_id` (`course_id`),
  KEY `idx_company_id` (`company_id`),
  KEY `idx_auditor_id` (`auditor_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程审核日志';

-- 机构备案表（平台审核机构/讲师/销售资质）
DROP TABLE IF EXISTS `platform_archive`;
CREATE TABLE `platform_archive` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `company_id` bigint NOT NULL COMMENT '机构ID',
  `archive_type` varchar(20) NOT NULL COMMENT '类型：org/teacher/sales',
  `name` varchar(100) NOT NULL COMMENT '名称',
  `contact_info` json DEFAULT NULL COMMENT '联系信息JSON：phone,wechat,qq,id_card,license_no等',
  `status` varchar(20) NOT NULL DEFAULT 'pending' COMMENT '状态：pending/approved/rejected',
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` varchar(64) DEFAULT NULL,
  `update_by` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_company_type` (`company_id`,`archive_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='平台备案（机构/讲师/销售）';

-- ----------------------------
-- 四、媒资模块
-- ----------------------------

-- 媒资文件表（MD5去重）
DROP TABLE IF EXISTS `media_file`;
CREATE TABLE `media_file` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `file_id` varchar(32) NOT NULL COMMENT '业务ID，对外暴露',
  `md5` varchar(32) NOT NULL COMMENT '文件MD5',
  `file_path` varchar(500) NOT NULL COMMENT 'MinIO对象路径：{md5[0]}/{md5[1]}/{md5}/{md5}_{fileName}.{ext}',
  `file_name` varchar(255) NOT NULL COMMENT '原始文件名',
  `file_size` bigint NOT NULL DEFAULT 0 COMMENT '文件大小(字节)',
  `file_type` varchar(20) DEFAULT NULL COMMENT 'video/doc/image/zip',
  `content_type` varchar(100) DEFAULT NULL COMMENT 'MIME类型',
  `bucket` varchar(64) NOT NULL DEFAULT 'xcplus' COMMENT 'MinIO bucket',
  `company_id` bigint NOT NULL COMMENT '机构ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_md5` (`md5`),
  UNIQUE KEY `uk_file_id` (`file_id`),
  KEY `idx_company_id` (`company_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='媒资文件（MD5去重）';

-- 分片上传任务表
DROP TABLE IF EXISTS `upload_task`;
CREATE TABLE `upload_task` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `upload_id` varchar(64) NOT NULL COMMENT '上传任务ID',
  `file_md5` varchar(32) NOT NULL,
  `file_name` varchar(255) NOT NULL,
  `file_size` bigint NOT NULL,
  `chunk_size` int NOT NULL COMMENT '分片大小',
  `chunk_count` int NOT NULL COMMENT '分片总数',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0进行中 1已完成',
  `company_id` bigint NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_upload_id` (`upload_id`),
  KEY `idx_company_id` (`company_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分片上传任务';

-- 已上传分片表
DROP TABLE IF EXISTS `upload_chunk`;
CREATE TABLE `upload_chunk` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `upload_id` varchar(64) NOT NULL,
  `chunk_index` int NOT NULL COMMENT '分片序号0-based',
  `chunk_path` varchar(500) NOT NULL COMMENT 'MinIO临时路径',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_upload_chunk` (`upload_id`,`chunk_index`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='已上传分片';

-- 课程计划-媒资关联表
DROP TABLE IF EXISTS `teachplan_media`;
CREATE TABLE `teachplan_media` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `media_id` varchar(32) DEFAULT NULL COMMENT '媒资ID（旧字段，兼容）',
  `teachplan_id` bigint NOT NULL COMMENT '计划ID',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `file_id` varchar(32) DEFAULT NULL COMMENT '关联media_file.file_id',
  `media_file_name` varchar(150) DEFAULT NULL COMMENT '媒资文件名',
  `media_type` varchar(20) DEFAULT 'video' COMMENT 'video/doc',
  `order_by` int NOT NULL DEFAULT 0 COMMENT '同节内排序',
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` varchar(64) DEFAULT NULL,
  `update_by` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_teachplan_id` (`teachplan_id`),
  KEY `idx_file_id` (`file_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程计划-媒资关联';

-- 课程购买记录表
DROP TABLE IF EXISTS `course_purchase`;
CREATE TABLE `course_purchase` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `order_id` varchar(64) DEFAULT NULL COMMENT '订单号',
  `purchase_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '购买时间',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_course` (`user_id`, `course_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_course_id` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程购买记录';

-- ----------------------------
-- 五、初始化数据
-- ----------------------------

-- 5.1 课程分类数据
INSERT INTO `course_category` VALUES
('1', '根节点', 'root', '0', 1, 0, 0, NOW(), NOW(), NULL, NULL),
('1-001', '编程开发', 'programming', '1', 1, 1, 0, NOW(), NOW(), NULL, NULL),
('1-001-001', 'Java', 'java', '1-001', 1, 1, 1, NOW(), NOW(), NULL, NULL),
('1-001-002', 'Python', 'python', '1-001', 1, 2, 1, NOW(), NOW(), NULL, NULL),
('1-001-003', 'C++', 'cpp', '1-001', 1, 3, 1, NOW(), NOW(), NULL, NULL),
('1-002', '人工智能', 'ai', '1', 1, 2, 0, NOW(), NOW(), NULL, NULL),
('1-002-001', '机器学习与深度学习', 'ml-dl', '1-002', 1, 1, 1, NOW(), NOW(), NULL, NULL),
('1-003', '嵌入式开发', 'embedded', '1', 1, 3, 1, NOW(), NOW(), NULL, NULL),
('1-004', '机器人技术', 'robotics', '1', 1, 4, 1, NOW(), NOW(), NULL, NULL);

-- 5.2 用户数据（密码：password，BCrypt: $2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG）
INSERT INTO `xc_user` (`username`, `password_hash`, `real_name`, `role`, `company_id`, `purchased_courses`, `status`) VALUES
-- SuperAdmin
('superadmin1', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', '系统管理员-张', 'super_admin', NULL, NULL, 1),
('superadmin2', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', '系统管理员-李', 'super_admin', NULL, NULL, 1),
-- 机构1 教师
('teacher1_org1', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', '学成-张老师', 'teacher', 1, NULL, 1),
('teacher2_org1', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', '学成-李老师', 'teacher', 1, NULL, 1),
-- 机构2 教师
('teacher1_org2', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', '极客-王老师', 'teacher', 2, NULL, 1),
('teacher2_org2', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', '极客-刘老师', 'teacher', 2, NULL, 1),
-- 访客/学员
('visitor1', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', '学员小明', 'visitor', NULL, '["1","2"]', 1),
('visitor2', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', '学员小红', 'visitor', NULL, NULL, 1);

-- 5.3 课程数据（涵盖所有状态：000草稿/100待审核/111已发布/112已下架）
-- 机构1 课程
INSERT INTO `course_base` (`company_id`, `company_name`, `name`, `users`, `tags`, `mt`, `st`, `grade`, `teach_mode`, `description`, `cover_pics`, `course_status`, `create_by`) VALUES
-- 111 已发布课程
(1, '学成教育', 'Java核心技术从入门到精通', '零基础学员、转行开发者', 'Java,后端,Spring', '1-001-001', '1-001-001', '204001', '200001', '系统讲解Java SE核心语法、面向对象、集合框架、IO流、多线程等。', NULL, '111', 3),
(1, '学成教育', 'Python数据分析与机器学习', '有编程基础、数据分析师', 'Python,AI,数据分析', '1-001-002', '1-002-001', '204002', '200001', '从NumPy、Pandas到Scikit-learn，掌握数据清洗、可视化与常见机器学习算法实战。', NULL, '111', 3),
(1, '学成教育', '大模型应用开发实战', 'Python基础、AI从业者', 'AI,LLM,大模型', '1-002', '1-002-001', '204003', '200002', '基于LangChain/LLaMA等框架，构建RAG、Agent与多模态应用。', NULL, '111', 3),
(1, '学成教育', 'TensorFlow2深度学习入门', 'Python基础', 'TensorFlow,深度学习,AI', '1-002', '1-002-001', '204002', '200001', 'Keras API、自定义层、模型部署，实战图像与NLP。', NULL, '111', 4),
(1, '学成教育', 'Arduino入门与物联网', '零基础、创客', 'Arduino,物联网,IoT', '1-003', '1-003', '204001', '200001', '传感器、执行器、无线通信，搭建智能家居原型。', NULL, '111', 4),
-- 100 待审核课程
(1, '学成教育', 'C++高性能编程与Qt实战', 'C/C++基础、嵌入式方向', 'C++,Qt,嵌入式', '1-001-003', '1-003', '204002', '200001', '深入C++11/14/17特性，内存管理、模板与STL，结合Qt开发跨平台桌面与嵌入式GUI应用。', NULL, '100', 3),
(1, '学成教育', 'Vue3+TypeScript前端实战', '有HTML/CSS基础', 'Vue,前端,TypeScript', '1-001-001', '1-001-001', '204002', '200001', '从Vue3组合式API到Pinia、Vue Router，构建企业级前端应用。', NULL, '100', 4),
-- 000 草稿课程
(1, '学成教育', 'STM32嵌入式系统开发', '电子/自动化专业、硬件爱好者', '嵌入式,STM32,单片机', '1-003', '1-003', '204002', '200001', '从GPIO、定时器、ADC到FreeRTOS，系统学习STM32外设与实时操作系统。', NULL, '000', 3),
(1, '学成教育', 'Docker与K8s容器化实战', 'Linux基础、运维/开发', 'Docker,K8s,DevOps', '1-001-001', '1-001-001', '204003', '200001', '容器镜像、编排、服务发现与负载均衡，生产级部署实践。', NULL, '000', 4),
-- 112 已下架课程
(1, '学成教育', 'ROS机器人操作系统入门', 'Linux基础、机械/自动化', '机器人,ROS,自动驾驶', '1-004', '1-004', '204002', '200001', '学习ROS1/ROS2架构，话题、服务、动作，搭建仿真环境。', NULL, '112', 3),
-- 机构2 课程
(2, '极客学院', 'Spring Boot 3微服务实战', 'Java基础、后端开发者', 'Java,Spring Boot,微服务', '1-001-001', '1-001-001', '204003', '200001', '基于Spring Boot 3、Spring Cloud Alibaba构建高可用微服务。', NULL, '111', 5),
(2, '极客学院', 'PyTorch深度学习实战', 'Python基础、AI工程师', 'PyTorch,深度学习,CV', '1-002', '1-002-001', '204003', '200001', '从张量、自动求导到CNN、RNN、Transformer，完成图像分类与目标检测。', NULL, '111', 5),
(2, '极客学院', 'React18全栈开发', 'JavaScript基础', 'React,全栈,Node', '1-001-001', '1-001-001', '204002', '200001', 'Hooks、状态管理、服务端渲染，构建完整前后端应用。', NULL, '100', 6),
(2, '极客学院', 'Rust系统编程入门', '有C/C++基础', 'Rust,系统编程,安全', '1-001-001', '1-001-001', '204003', '200001', '所有权、借用、生命周期，编写安全高性能系统。', NULL, '000', 6),
(2, '极客学院', 'Kafka消息队列实战', 'Java基础、分布式', 'Kafka,消息队列,大数据', '1-001-001', '1-001-001', '204003', '200001', '生产者、消费者、Topic、分区、副本与故障转移。', NULL, '112', 5);

-- 5.4 课程营销数据
INSERT INTO `course_market` (`id`, `charge`, `price`, `original_price`, `qq`, `wechat`, `phone`, `valid_days`) VALUES
-- 免费课（charge=201000）
(1, '201000', 0.00, 0.00, '80012345', 'xc_service', '13800001001', 365),
(2, '201000', 0.00, 0.00, '80012346', 'xc_data_ai', '13800001002', 365),
(11, '201000', 0.00, 0.00, '80012355', 'xc_iot', '13800001011', 365),
-- 收费课（charge=201001）
(3, '201001', 599.00, 799.00, '80012347', 'xc_llm', '13800001003', 365),
(4, '201001', 499.00, 699.00, '80012348', 'xc_tf', '13800001004', 365),
(5, '201001', 399.00, 499.00, '80012349', 'xc_cpp', '13800001005', 365),
(6, '201001', 299.00, 399.00, '80012350', 'xc_vue', '13800001006', 365),
(7, '201001', 599.00, 799.00, '80012351', 'xc_stm32', '13800001007', 365),
(8, '201001', 499.00, 699.00, '80012352', 'xc_docker', '13800001008', 365),
(9, '201001', 449.00, 549.00, '80012353', 'xc_ros', '13800001009', 365),
(10, '201001', 499.00, 699.00, '80012354', 'jk_spring', '13800001010', 365),
(12, '201001', 599.00, 799.00, '80012356', 'jk_pytorch', '13800001012', 365),
(13, '201001', 399.00, 499.00, '80012357', 'jk_react', '13800001013', 365),
(14, '201001', 299.00, 399.00, '80012358', 'jk_rust', '13800001014', 365),
(15, '201001', 349.00, 449.00, '80012359', 'jk_kafka', '13800001015', 365);

-- 5.5 课程教师数据
INSERT INTO `course_teacher` (`course_id`, `teacher_name`, `position`, `description`) VALUES
(1, '张老师', '高级讲师', '10年Java开发经验，曾任职阿里、美团，精通Spring生态与分布式架构。'),
(1, '李老师', '技术顾问', '《Java并发编程实战》译者，专注JVM与高并发。'),
(2, '王老师', 'AI工程师', '硕士毕业于中科院，5年机器学习与深度学习项目经验。'),
(3, '陈老师', '大模型架构师', '参与多个LLM落地项目，熟悉RAG与Agent开发。'),
(4, '吴老师', 'AI研究员', 'PyTorch核心贡献者之一，专注计算机视觉。'),
(5, '赵老师', '嵌入式讲师', 'STM32与FreeRTOS资深讲师，出版多本嵌入式教材。'),
(6, '刘老师', 'C++专家', '前华为嵌入式工程师，擅长C/C++与实时系统。'),
(7, '冯老师', '前端架构师', 'Vue/React技术专家，前端工程化实践者。'),
(8, '褚老师', 'DevOps工程师', 'K8s CKA认证，CI/CD与云原生实践。'),
(9, '周老师', '机器人工程师', 'ROS社区贡献者，自动驾驶与机械臂方向。'),
(10, '郑老师', '微服务专家', '10年分布式系统经验，精通Spring Cloud。'),
(11, '卫老师', 'Go语言讲师', 'Go社区贡献者，高并发系统设计专家。'),
(12, '蒋老师', 'AI讲师', 'TensorFlow认证讲师，深度学习课程负责人。'),
(13, '秦老师', '全栈工程师', 'React与Node.js全栈开发。'),
(14, '尤老师', 'Rust讲师', 'Rust中文社区核心成员。'),
(15, '韩老师', '大数据工程师', 'Kafka与流式计算专家。');

-- 5.6 课程计划数据（课程1：Java核心技术）
INSERT INTO `teachplan` (`pname`, `parent_id`, `grade`, `order_by`, `course_id`, `status`, `is_preview_enabled`) VALUES
-- 章
('第1章 Java基础', 0, 1, 1, 1, 1, '0'),
('第2章 面向对象', 0, 1, 2, 1, 1, '0'),
('第3章 集合与IO', 0, 1, 3, 1, 1, '0'),
-- 第1章节
('1.1 Java环境搭建', 1, 2, 1, 1, 1, '1'),
('1.2 变量与数据类型', 1, 2, 2, 1, 1, '0'),
('1.3 流程控制', 1, 2, 3, 1, 1, '0'),
-- 第2章节
('2.1 类与对象', 2, 2, 1, 1, 1, '1'),
('2.2 继承与多态', 2, 2, 2, 1, 1, '0'),
-- 第3章节
('3.1 集合框架', 3, 2, 1, 1, 1, '1'),
('3.2 IO流', 3, 2, 2, 1, 1, '0');

-- 5.6b 课程购买记录（visitor1 已购买课程1、2）
INSERT INTO `course_purchase` (`user_id`, `course_id`, `order_id`) VALUES
(7, 1, 'ORD001'),
(7, 2, 'ORD002');

-- 5.7 站内信测试数据
INSERT INTO `system_message` (`type`, `title`, `content`, `course_id`, `course_name`, `from_user_id`, `from_user_name`, `to_role`, `is_read`) VALUES
('COURSE_SUBMIT', '新课程待审核', '机构【学成教育】提交了新课程《C++高性能编程与Qt实战》，请及时审核。', 6, 'C++高性能编程与Qt实战', 3, '学成-张老师', 'super_admin', 0),
('COURSE_SUBMIT', '新课程待审核', '机构【学成教育】提交了新课程《Vue3+TypeScript前端实战》，请及时审核。', 7, 'Vue3+TypeScript前端实战', 4, '学成-李老师', 'super_admin', 0),
('COURSE_SUBMIT', '新课程待审核', '机构【极客学院】提交了新课程《React18全栈开发》，请及时审核。', 13, 'React18全栈开发', 6, '极客-王老师', 'super_admin', 0);

-- 5.8 审核日志测试数据
INSERT INTO `course_audit_log` (`course_id`, `course_name`, `company_id`, `company_name`, `audit_action`, `audit_opinion`, `auditor_id`, `auditor_name`, `old_status`, `new_status`) VALUES
(9, 'ROS机器人操作系统入门', 1, '学成教育', 'ban', '内容质量不符合平台要求，且存在版权问题', 1, '系统管理员-张', '100', '112'),
(15, 'Kafka消息队列实战', 2, '极客学院', 'offline', '机构申请下架', 5, '极客-王老师', '111', '112'),
(1, 'Java核心技术从入门到精通', 1, '学成教育', 'approve', '内容完整，质量优秀', 1, '系统管理员-张', '100', '111'),
(2, 'Python数据分析与机器学习', 1, '学成教育', 'approve', '课程结构合理，实操性强', 2, '系统管理员-李', '100', '111');

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- 执行完成提示
-- ----------------------------
SELECT 'v3.0-new-student 初始化完成！' AS message;
SELECT CONCAT('用户总数: ', COUNT(*)) FROM xc_user;
SELECT CONCAT('课程总数: ', COUNT(*)) FROM course_base;
SELECT course_status, COUNT(*) AS count FROM course_base GROUP BY course_status;
