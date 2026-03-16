package com.xuecheng.content.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程审核日志
 */
@Data
@TableName("course_audit_log")
public class CourseAuditLog {

    @TableId
    private Long id;
    private Long courseId;
    private String courseName;
    private Long companyId;
    private String companyName;
    private String auditAction;
    private String auditOpinion;
    private Long auditorId;
    private String auditorName;
    private String oldStatus;
    private String newStatus;
    private LocalDateTime createTime;
}
