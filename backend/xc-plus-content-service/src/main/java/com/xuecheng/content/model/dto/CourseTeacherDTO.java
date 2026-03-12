package com.xuecheng.content.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 教师信息
 */
@Data
@Schema(description = "课程教师信息")
public class CourseTeacherDTO {

    @Schema(description = "教师姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String teacherName;
    @Schema(description = "职位")
    private String position;
    @Schema(description = "教师简介")
    private String description;
    @Schema(description = "照片URL")
    private String photograph;
}
