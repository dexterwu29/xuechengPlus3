package com.xuecheng.content.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 课程分页查询请求体（含分页+条件）
 */
@Data
@Schema(description = "课程分页查询请求")
public class CoursePageQueryDTO {

    @Schema(description = "页码", example = "1")
    private Integer pageNo = 1;
    @Schema(description = "每页条数", example = "10")
    private Integer pageSize = 10;
    @Schema(description = "课程名称（模糊）")
    private String courseName;
    @Schema(description = "审核状态：202001未通过 202002未提交 202003已提交 202004通过")
    private String auditStatus;
    @Schema(description = "发布状态：203001未发布 203002已发布 203003下线")
    private String publishStatus;
}
