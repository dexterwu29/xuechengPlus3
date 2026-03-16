/*
 学成在线三代 - 机构/讲师/销售备案表
 执行前需已执行 xcplus_content.sql
*/
USE `xcplus_content`;

CREATE TABLE IF NOT EXISTS `platform_archive` (
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
