/*
 学成在线三代 - 媒资与上传扩展
 执行前需已执行 xcplus_content.sql
*/

USE `xcplus_content`;

-- ----------------------------
-- media_file 媒资文件表（MD5 去重）
-- ----------------------------
DROP TABLE IF EXISTS `media_file`;
CREATE TABLE `media_file` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `file_id` varchar(32) NOT NULL COMMENT '业务ID，对外暴露',
  `md5` varchar(32) NOT NULL COMMENT '文件MD5',
  `file_path` varchar(500) NOT NULL COMMENT 'MinIO对象路径',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='媒资文件(MD5去重)';

-- ----------------------------
-- upload_task 分片上传任务
-- ----------------------------
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

-- ----------------------------
-- upload_chunk 已上传分片
-- ----------------------------
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

-- ----------------------------
-- teachplan_media 扩展：增加 file_id、media_type、order_by
-- ----------------------------
ALTER TABLE `teachplan_media` ADD COLUMN `file_id` varchar(32) DEFAULT NULL COMMENT '关联media_file.file_id' AFTER `media_id`;
ALTER TABLE `teachplan_media` ADD COLUMN `media_type` varchar(20) DEFAULT 'video' COMMENT 'video/doc' AFTER `media_file_name`;
ALTER TABLE `teachplan_media` ADD COLUMN `order_by` int NOT NULL DEFAULT 0 COMMENT '同节内排序' AFTER `media_type`;
