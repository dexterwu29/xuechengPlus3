package com.xuecheng.content.model.dto;

import lombok.Data;

/**
 * 新增课程请求体
 */
@Data
public class CourseCreateDTO {

    private String name;
    private String users;
    private String tags;
    private String mt;
    private String st;
    private String grade;
    private String teachMode;
    private String description;
    private String pic;
}
