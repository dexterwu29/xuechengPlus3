package com.xuecheng.content.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程计划-媒资关联实体
 */
@Data
@TableName("teachplan_media")
public class TeachplanMedia {

    @TableId
    private Long id;
    @TableField("media_id")
    private String mediaId;
    @TableField("teachplan_id")
    private Long teachplanId;
    @TableField("course_id")
    private Long courseId;
    @TableField("media_file_name")
    private String mediaFileName;
    @TableLogic
    private Integer isDeleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String createBy;
    private String updateBy;
}
