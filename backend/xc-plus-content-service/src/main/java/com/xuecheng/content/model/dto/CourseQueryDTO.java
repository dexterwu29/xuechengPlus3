package com.xuecheng.content.model.dto;

import lombok.Data;

/**
 * 课程分页查询条件
 */
@Data
public class CourseQueryDTO {

    /** 课程名称 */
    private String courseName;
    /** 审核状态 */
    private String auditStatus;
    /** 发布状态 */
    private String publishStatus;
}
