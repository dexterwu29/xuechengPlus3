/*
 学成在线三代 - 用户与权限扩展
 执行前需已执行 xcplus_content.sql 和 xcplus_content_media.sql
 角色: super_admin(系统管理员), teacher(机构教师), visitor(访客/学员)
 密码统一为: password
 BCrypt 哈希: $2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG
*/

USE `xcplus_content`;

-- ----------------------------
-- xc_user 用户表
-- ----------------------------
DROP TABLE IF EXISTS `xc_user`;
CREATE TABLE `xc_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(64) NOT NULL COMMENT '登录名',
  `password_hash` varchar(128) NOT NULL COMMENT 'BCrypt 密码哈希',
  `real_name` varchar(64) DEFAULT NULL COMMENT '真实姓名',
  `role` varchar(32) NOT NULL COMMENT 'super_admin/teacher/visitor',
  `company_id` bigint DEFAULT NULL COMMENT '机构ID，teacher 必填',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1启用 0禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_company_role` (`company_id`, `role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户';

-- ----------------------------
-- course_purchase 课程购买记录（用于判断访客是否已付费某课程）
-- ----------------------------
DROP TABLE IF EXISTS `course_purchase`;
CREATE TABLE `course_purchase` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `order_id` varchar(64) DEFAULT NULL COMMENT '订单号',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_course` (`user_id`, `course_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_course_id` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程购买';

-- ----------------------------
-- 测试账号（密码均为 password）
-- BCrypt: $2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG
-- ----------------------------
INSERT INTO `xc_user` (`username`, `password_hash`, `real_name`, `role`, `company_id`, `status`) VALUES
-- SuperAdmin 2个
('superadmin1', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', '系统管理员1', 'super_admin', NULL, 1),
('superadmin2', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', '系统管理员2', 'super_admin', NULL, 1),
-- 机构1 教师 2个
('teacher1_org1', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', '学成-张老师', 'teacher', 1, 1),
('teacher2_org1', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', '学成-李老师', 'teacher', 1, 1),
-- 机构2 教师 2个
('teacher1_org2', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', '极客-王老师', 'teacher', 2, 1),
('teacher2_org2', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', '极客-刘老师', 'teacher', 2, 1),
-- 访客/学员 2个
('visitor1', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', '学员小明', 'visitor', NULL, 1),
('visitor2', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', '学员小红', 'visitor', NULL, 1);
