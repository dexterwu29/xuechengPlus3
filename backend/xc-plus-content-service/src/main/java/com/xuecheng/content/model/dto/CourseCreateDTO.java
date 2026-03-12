package com.xuecheng.content.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 新增课程请求体
 */
@Data
@Schema(description = "新增课程请求")
public class CourseCreateDTO {

    @Schema(description = "课程名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
    @Schema(description = "适用人群")
    private String users;
    @Schema(description = "课程标签")
    private String tags;
    @Schema(description = "大分类ID")
    private String mt;
    @Schema(description = "小分类ID")
    private String st;
    @Schema(description = "课程等级：204001初级 204002中级 204003高级")
    private String grade;
    @Schema(description = "教育模式：200001普通 200002录播 200003直播")
    private String teachMode;
    @Schema(description = "课程介绍")
    private String description;
    @Schema(description = "课程图片URL")
    private String pic;
}
