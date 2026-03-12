package com.xuecheng.content.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程基本信息实体
 */
@Data
@TableName("course_base")
public class CourseBase {

    @TableId
    private Long id;
    private Long companyId;
    private String companyName;
    private String name;
    private String users;
    private String tags;
    private String mt;
    private String st;
    private String grade;
    @TableField("teach_mode")
    private String teachMode;
    private String description;
    private String pic;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String createBy;
    private String updateBy;
    private String auditStatus;
    @TableField("publish_status")
    private String publishStatus;
    @TableLogic
    private Integer isDeleted;
}
