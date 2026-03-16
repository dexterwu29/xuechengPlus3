package com.xuecheng.content.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 课程分页查询条件
 */
@Data
@Schema(description = "课程查询条件")
public class CourseQueryDTO {

    @Schema(description = "课程名称（模糊）")
    private String courseName;
    @Schema(description = "课程状态：000草稿 100待审核 111已发布 112已下架")
    private String courseStatus;
    @Schema(description = "大分类ID，如 1-001")
    private String mt;
    @Schema(description = "小分类ID，如 1-001-001")
    private String st;
}
