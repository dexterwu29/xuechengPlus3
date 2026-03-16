/*
 学成在线三代 - 31.4批次：添加视频时长支持
 执行前需确保已执行 v3_new_student_init.sql
*/

USE `xcplus_content`;

-- ----------------------------
-- 添加视频时长字段到 media_file 表
-- ----------------------------
ALTER TABLE `media_file` ADD COLUMN `duration` int DEFAULT NULL COMMENT '视频时长(秒)，仅视频类型有效' AFTER `file_type`;

-- ----------------------------
-- 添加视频时长字段到 teachplan_media 表（冗余存储，便于查询）
-- ----------------------------
ALTER TABLE `teachplan_media` ADD COLUMN `duration` int DEFAULT NULL COMMENT '视频时长(秒)，仅视频类型有效' AFTER `media_type`;
