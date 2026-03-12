package com.xuecheng.content.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 教师信息
 */
@Data
@Schema(description = "课程教师信息")
public class CourseTeacherDTO {

    @NotBlank(message = "教师姓名不能为空")
    @Size(max = 60, message = "教师姓名不能超过60字符")
    @Schema(description = "教师姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String teacherName;

    @Size(max = 255, message = "职位不能超过255字符")
    @Schema(description = "职位")
    private String position;

    @Size(max = 1024, message = "教师简介不能超过1024字符")
    @Schema(description = "教师简介")
    private String description;

    @Size(max = 1024, message = "照片URL不能超过1024字符")
    @Schema(description = "照片URL")
    private String photograph;
}
