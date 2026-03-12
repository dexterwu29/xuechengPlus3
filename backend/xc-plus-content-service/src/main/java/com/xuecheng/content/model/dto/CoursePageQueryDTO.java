package com.xuecheng.content.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 课程分页查询请求体（含分页+条件）
 */
@Data
@Schema(description = "课程分页查询请求")
public class CoursePageQueryDTO {

    @Min(value = 1, message = "页码不能小于1")
    @Max(value = 9999, message = "页码不能超过9999")
    @Schema(description = "页码", example = "1")
    private Integer pageNo = 1;

    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 100, message = "每页条数不能超过100")
    @Schema(description = "每页条数", example = "10")
    private Integer pageSize = 10;

    @Size(max = 500, message = "课程名称不能超过500字符")
    @Schema(description = "课程名称（模糊）")
    private String courseName;

    @Pattern(regexp = "^(202001|202002|202003|202004)?$", message = "审核状态必须为202001/202002/202003/202004")
    @Schema(description = "审核状态：202001未通过 202002未提交 202003已提交 202004通过")
    private String auditStatus;

    @Pattern(regexp = "^(203001|203002|203003)?$", message = "发布状态必须为203001/203002/203003")
    @Schema(description = "发布状态：203001未发布 203002已发布 203003下线")
    private String publishStatus;

    @Size(max = 20, message = "大分类ID不能超过20字符")
    @Schema(description = "大分类ID，如 1-001")
    private String mt;

    @Size(max = 20, message = "小分类ID不能超过20字符")
    @Schema(description = "小分类ID，如 1-001-001")
    private String st;
}
