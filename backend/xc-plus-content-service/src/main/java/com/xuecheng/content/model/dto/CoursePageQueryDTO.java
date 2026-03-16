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

    @Pattern(regexp = "^(000|100|111|112)?$", message = "课程状态必须为000/100/111/112")
    @Schema(description = "课程状态：000草稿 100待审核 111已发布 112已下架")
    private String courseStatus;

    @Size(max = 20, message = "大分类ID不能超过20字符")
    @Schema(description = "大分类ID，如 1-001")
    private String mt;

    @Size(max = 20, message = "小分类ID不能超过20字符")
    @Schema(description = "小分类ID，如 1-001-001")
    private String st;
}
