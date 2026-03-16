/*
 学成在线三代 - 课程多封面扩展
 执行前需已执行 xcplus_content.sql
*/
USE `xcplus_content`;

ALTER TABLE `course_base`
  ADD COLUMN `cover_pics` TEXT DEFAULT NULL COMMENT '封面图fileId数组JSON，如["media:xxx","media:yyy"]，最多3个' AFTER `pic`,
  ADD COLUMN `default_cover_index` TINYINT NOT NULL DEFAULT 0 COMMENT '默认展示的封面索引0-2' AFTER `cover_pics`;
