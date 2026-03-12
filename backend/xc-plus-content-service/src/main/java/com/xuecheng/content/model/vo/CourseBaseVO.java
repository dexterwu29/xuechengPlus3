package com.xuecheng.content.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程列表项 VO
 */
@Data
public class CourseBaseVO {

    private Long id;
    private Long companyId;
    private String companyName;
    private String name;
    private String users;
    private String tags;
    private String mt;
    private String st;
    private String grade;
    private String teachMode;
    private String description;
    private String pic;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String auditStatus;
    private String status;
}
