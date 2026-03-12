package com.xuecheng.content.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程计划实体（章/节）
 */
@Data
@TableName("teachplan")
public class Teachplan {

    @TableId
    private Long id;
    private String pname;
    @TableField("parent_id")
    private Long parentId;
    private Integer grade;
    @TableField("media_type")
    private String mediaType;
    @TableField("order_by")
    private Integer orderBy;
    @TableField("course_id")
    private Long courseId;
    private Integer status;
    @TableField("is_preview_enabled")
    private String isPreviewEnabled;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String createBy;
    private String updateBy;
    @TableLogic
    private Integer isDeleted;
}
