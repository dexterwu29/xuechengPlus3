/*
 学成在线三代 - 课程管理模块初始化脚本
 数据库: xcplus_content
 表: course_base, course_category, course_market, course_teacher, teachplan, teachplan_media
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 创建数据库
-- ----------------------------
CREATE DATABASE IF NOT EXISTS `xcplus_content` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `xcplus_content`;

-- ----------------------------
-- Table structure for course_category
-- ----------------------------
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

-- ----------------------------
-- Table structure for course_base
-- ----------------------------
DROP TABLE IF EXISTS `course_base`;
CREATE TABLE `course_base` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `company_id` bigint NOT NULL COMMENT '机构ID',
  `company_name` varchar(255) DEFAULT NULL COMMENT '机构名称',
  `name` varchar(100) NOT NULL COMMENT '课程名称',
  `users` varchar(500) DEFAULT NULL COMMENT '适用人群',
  `tags` varchar(50) DEFAULT NULL COMMENT '课程标签',
  `mt` varchar(20) DEFAULT NULL COMMENT '大分类ID',
  `st` varchar(20) DEFAULT NULL COMMENT '小分类ID',
  `grade` varchar(32) DEFAULT NULL COMMENT '课程等级',
  `teach_mode` varchar(32) DEFAULT NULL COMMENT '教育模式',
  `description` text COMMENT '课程介绍',
  `pic` varchar(500) DEFAULT NULL COMMENT '课程图片',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` varchar(50) DEFAULT NULL,
  `update_by` varchar(50) DEFAULT NULL,
  `audit_status` varchar(10) NOT NULL DEFAULT '202002' COMMENT '审核状态',
  `status` varchar(10) NOT NULL DEFAULT '203001' COMMENT '发布状态',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '是否已删除',
  PRIMARY KEY (`id`),
  KEY `idx_company_id` (`company_id`),
  KEY `idx_mt_st` (`mt`,`st`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程基本信息';

-- ----------------------------
-- Table structure for course_market
-- ----------------------------
DROP TABLE IF EXISTS `course_market`;
CREATE TABLE `course_market` (
  `id` bigint NOT NULL COMMENT '主键，课程ID',
  `charge` varchar(32) DEFAULT NULL COMMENT '收费规则',
  `price` float DEFAULT NULL COMMENT '现价',
  `original_price` float DEFAULT NULL COMMENT '原价',
  `qq` varchar(32) DEFAULT NULL COMMENT '咨询QQ',
  `wechat` varchar(64) DEFAULT NULL COMMENT '微信',
  `phone` varchar(32) DEFAULT NULL COMMENT '电话',
  `valid_days` int DEFAULT NULL COMMENT '有效期天数',
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` varchar(64) DEFAULT NULL,
  `update_by` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程营销';

-- ----------------------------
-- Table structure for course_teacher
-- ----------------------------
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

-- ----------------------------
-- Table structure for teachplan
-- ----------------------------
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
  `is_preview_enabled` char(1) DEFAULT '0' COMMENT '是否可试看',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` varchar(64) DEFAULT NULL,
  `update_by` varchar(64) DEFAULT NULL,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_course_id` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程计划';

-- ----------------------------
-- Table structure for teachplan_media
-- ----------------------------
DROP TABLE IF EXISTS `teachplan_media`;
CREATE TABLE `teachplan_media` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `media_id` varchar(32) DEFAULT NULL COMMENT '媒资ID',
  `teachplan_id` bigint NOT NULL COMMENT '计划ID',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `media_file_name` varchar(150) DEFAULT NULL COMMENT '媒资文件名',
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` varchar(64) DEFAULT NULL,
  `update_by` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_teachplan_id` (`teachplan_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='计划-媒资';

-- ----------------------------
-- Records of course_category (树形：根1，编程2，AI3，嵌入式4，机器人5)
-- ----------------------------
INSERT INTO `course_category` VALUES ('1', '根节点', 'root', '0', 1, 0, 0, NOW(), NOW(), NULL, NULL);
INSERT INTO `course_category` VALUES ('1-001', '编程开发', 'programming', '1', 1, 1, 0, NOW(), NOW(), NULL, NULL);
INSERT INTO `course_category` VALUES ('1-001-001', 'Java', 'java', '1-001', 1, 1, 1, NOW(), NOW(), NULL, NULL);
INSERT INTO `course_category` VALUES ('1-001-002', 'Python', 'python', '1-001', 1, 2, 1, NOW(), NOW(), NULL, NULL);
INSERT INTO `course_category` VALUES ('1-001-003', 'C++', 'cpp', '1-001', 1, 3, 1, NOW(), NOW(), NULL, NULL);
INSERT INTO `course_category` VALUES ('1-002', '人工智能', 'ai', '1', 1, 2, 0, NOW(), NOW(), NULL, NULL);
INSERT INTO `course_category` VALUES ('1-002-001', '机器学习与深度学习', 'ml-dl', '1-002', 1, 1, 1, NOW(), NOW(), NULL, NULL);
INSERT INTO `course_category` VALUES ('1-003', '嵌入式开发', 'embedded', '1', 1, 3, 1, NOW(), NOW(), NULL, NULL);
INSERT INTO `course_category` VALUES ('1-004', '机器人技术', 'robotics', '1', 1, 4, 1, NOW(), NOW(), NULL, NULL);

-- ----------------------------
-- Records of course_base (Company 1 and 2, semantic mock data)
-- ----------------------------
INSERT INTO `course_base` (`company_id`, `company_name`, `name`, `users`, `tags`, `mt`, `st`, `grade`, `teach_mode`, `description`, `pic`, `audit_status`, `status`) VALUES
(1, '学成教育', 'Java核心技术从入门到精通', '零基础学员、转行开发者', 'Java,后端,Spring', '1-001-001', '1-001-001', '204001', '200001', '系统讲解Java SE核心语法、面向对象、集合框架、IO流、多线程等，为Spring/微服务打下坚实基础。', NULL, '202002', '203001'),
(1, '学成教育', 'Python数据分析与机器学习', '有编程基础、数据分析师', 'Python,AI,数据分析', '1-001-002', '1-002-001', '204002', '200001', '从NumPy、Pandas到Scikit-learn，掌握数据清洗、可视化与常见机器学习算法实战。', NULL, '202002', '203001'),
(1, '学成教育', 'C++高性能编程与Qt实战', 'C/C++基础、嵌入式方向', 'C++,Qt,嵌入式', '1-001-003', '1-003', '204002', '200001', '深入C++11/14/17特性，内存管理、模板与STL，结合Qt开发跨平台桌面与嵌入式GUI应用。', NULL, '202002', '203001'),
(1, '学成教育', '大模型应用开发实战', 'Python基础、AI从业者', 'AI,LLM,大模型', '1-002', '1-002-001', '204003', '200002', '基于LangChain/LLaMA等框架，构建RAG、Agent与多模态应用，掌握Prompt工程与模型微调。', NULL, '202002', '203001'),
(1, '学成教育', 'STM32嵌入式系统开发', '电子/自动化专业、硬件爱好者', '嵌入式,STM32,单片机', '1-003', '1-003', '204002', '200001', '从GPIO、定时器、ADC到FreeRTOS，系统学习STM32外设与实时操作系统，完成智能硬件项目。', NULL, '202002', '203001'),
(1, '学成教育', 'ROS机器人操作系统入门', 'Linux基础、机械/自动化', '机器人,ROS,自动驾驶', '1-004', '1-004', '204002', '200001', '学习ROS1/ROS2架构，话题、服务、动作，搭建仿真环境，实现导航与机械臂控制。', NULL, '202002', '203001'),
(2, '极客学院', 'Spring Boot 3微服务实战', 'Java基础、后端开发者', 'Java,Spring Boot,微服务', '1-001-001', '1-001-001', '204003', '200001', '基于Spring Boot 3、Spring Cloud Alibaba构建高可用微服务，涵盖Nacos、Sentinel、Seata。', NULL, '202002', '203001'),
(2, '极客学院', 'PyTorch深度学习实战', 'Python基础、AI工程师', 'PyTorch,深度学习,CV', '1-002', '1-002-001', '204003', '200001', '从张量、自动求导到CNN、RNN、Transformer，完成图像分类、目标检测与NLP项目。', NULL, '202002', '203001');

-- ----------------------------
-- Records of course_market (对应上述课程)
-- ----------------------------
INSERT INTO `course_market` (`id`, `charge`, `price`, `original_price`, `valid_days`) VALUES
(1, '201001', 199.00, 299.00, 365),
(2, '201001', 299.00, 399.00, 365),
(3, '201001', 399.00, 499.00, 365),
(4, '201001', 599.00, 799.00, 365),
(5, '201001', 349.00, 449.00, 365),
(6, '201001', 449.00, 549.00, 365),
(7, '201001', 499.00, 699.00, 365),
(8, '201000', 0.00, 0.00, 365);

-- ----------------------------
-- Records of course_teacher
-- ----------------------------
INSERT INTO `course_teacher` (`course_id`, `teacher_name`, `position`, `description`) VALUES
(1, '张老师', '高级讲师', '10年Java开发经验，曾任职阿里、美团，精通Spring生态与分布式架构。'),
(1, '李老师', '技术顾问', '《Java并发编程实战》译者，专注JVM与高并发。'),
(2, '王老师', 'AI工程师', '硕士毕业于中科院，5年机器学习与深度学习项目经验。'),
(3, '刘老师', '嵌入式专家', '前华为嵌入式工程师，擅长C/C++与实时系统。'),
(4, '陈老师', '大模型架构师', '参与多个LLM落地项目，熟悉RAG与Agent开发。'),
(5, '赵老师', '嵌入式讲师', 'STM32与FreeRTOS资深讲师，出版多本嵌入式教材。'),
(6, '孙老师', '机器人工程师', 'ROS社区贡献者，自动驾驶与机械臂方向。'),
(7, '周老师', '架构师', 'Spring Cloud Alibaba技术布道师，微服务实战专家。'),
(8, '吴老师', 'AI研究员', 'PyTorch核心贡献者之一，专注计算机视觉。');

-- ----------------------------
-- Records of teachplan (章/节，以课程1为例)
-- ----------------------------
INSERT INTO `teachplan` (`pname`, `parent_id`, `grade`, `order_by`, `course_id`, `status`) VALUES
('第1章 Java基础', 0, 1, 1, 1, 1),
('第2章 面向对象', 0, 1, 2, 1, 1),
('第3章 集合与IO', 0, 1, 3, 1, 1),
('1.1 Java环境搭建', 1, 2, 1, 1, 1),
('1.2 变量与数据类型', 1, 2, 2, 1, 1),
('1.3 流程控制', 1, 2, 3, 1, 1),
('2.1 类与对象', 2, 2, 1, 1, 1),
('2.2 继承与多态', 2, 2, 2, 1, 1);

-- 课程2的章
INSERT INTO `teachplan` (`pname`, `parent_id`, `grade`, `order_by`, `course_id`, `status`) VALUES
('第1章 NumPy与Pandas', 0, 1, 1, 2, 1),
('第2章 数据可视化', 0, 1, 2, 2, 1),
('第3章 机器学习入门', 0, 1, 3, 2, 1);

-- 课程3的章
INSERT INTO `teachplan` (`pname`, `parent_id`, `grade`, `order_by`, `course_id`, `status`) VALUES
('第1章 C++11新特性', 0, 1, 1, 3, 1),
('第2章 Qt基础', 0, 1, 2, 3, 1);

SET FOREIGN_KEY_CHECKS = 1;
