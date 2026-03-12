package com.xuecheng.content.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程教师实体
 */
@Data
@TableName("course_teacher")
public class CourseTeacher {

    @TableId
    private Long id;
    @TableField("course_id")
    private Long courseId;
    @TableField("teacher_name")
    private String teacherName;
    private String position;
    private String description;
    private String photograph;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String createBy;
    private String updateBy;
    @TableLogic
    private Integer isDeleted;
}
