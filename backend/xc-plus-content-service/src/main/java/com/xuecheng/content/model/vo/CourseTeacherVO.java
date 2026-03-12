package com.xuecheng.content.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 教师信息 VO
 */
@Data
@Schema(description = "课程教师信息")
public class CourseTeacherVO {

    @Schema(description = "教师ID")
    private Long id;
    @Schema(description = "课程ID")
    private Long courseId;
    @Schema(description = "教师姓名")
    private String teacherName;
    @Schema(description = "职位")
    private String position;
    @Schema(description = "教师简介")
    private String description;
    @Schema(description = "照片URL")
    private String photograph;
    @Schema(description = "创建时间（东八区）")
    private LocalDateTime createTime;
    @Schema(description = "更新时间（东八区）")
    private LocalDateTime updateTime;
}
