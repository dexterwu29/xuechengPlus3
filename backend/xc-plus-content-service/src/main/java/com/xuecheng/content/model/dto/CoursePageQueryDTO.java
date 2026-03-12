package com.xuecheng.content.model.dto;

import lombok.Data;

/**
 * 课程分页查询请求体（含分页+条件）
 */
@Data
public class CoursePageQueryDTO {

    private Integer pageNo = 1;
    private Integer pageSize = 10;
    private String courseName;
    private String auditStatus;
    private String publishStatus;
}
