package com.xuecheng.content.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 教师信息 VO
 */
@Data
public class CourseTeacherVO {

    private Long id;
    private Long courseId;
    private String teacherName;
    private String position;
    private String description;
    private String photograph;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
