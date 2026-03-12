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
    @Schema(description = "审核状态：202001未通过 202002未提交 202003已提交 202004通过")
    private String auditStatus;
    @Schema(description = "发布状态：203001未发布 203002已发布 203003下线")
    private String publishStatus;
    @Schema(description = "大分类ID，如 1-001")
    private String mt;
    @Schema(description = "小分类ID，如 1-001-001")
    private String st;
}
