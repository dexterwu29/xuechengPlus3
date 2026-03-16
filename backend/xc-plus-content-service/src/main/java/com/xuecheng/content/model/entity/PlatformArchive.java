package com.xuecheng.content.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 平台备案（机构/讲师/销售）
 */
@Data
@TableName("platform_archive")
public class PlatformArchive {

    @TableId
    private Long id;
    @TableField("company_id")
    private Long companyId;
    @TableField("archive_type")
    private String archiveType;  // org / teacher / sales
    private String name;
    @TableField("contact_info")
    private String contactInfo;  // JSON
    private String status;  // pending / approved / rejected
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
    @TableField("create_time")
    private LocalDateTime createTime;
    @TableField("update_time")
    private LocalDateTime updateTime;
    @TableField("create_by")
    private String createBy;
    @TableField("update_by")
    private String updateBy;
}
