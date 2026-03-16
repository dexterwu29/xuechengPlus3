package com.xuecheng.content.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新增课程请求体
 */
@Data
@Schema(description = "新增课程请求")
public class CourseCreateDTO {

    @NotBlank(message = "课程名称不能为空")
    @Size(max = 100, message = "课程名称不能超过100字符")
    @Schema(description = "课程名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Size(max = 500, message = "适用人群不能超过500字符")
    @Schema(description = "适用人群")
    private String users;

    @Size(max = 50, message = "课程标签不能超过50字符")
    @Schema(description = "课程标签")
    private String tags;

    @Size(max = 20, message = "大分类ID不能超过20字符")
    @Schema(description = "大分类ID")
    private String mt;

    @Size(max = 20, message = "小分类ID不能超过20字符")
    @Schema(description = "小分类ID")
    private String st;

    @Pattern(regexp = "^(204001|204002|204003)?$", message = "课程等级必须为204001/204002/204003")
    @Schema(description = "课程等级：204001初级 204002中级 204003高级")
    private String grade;

    @Pattern(regexp = "^(200001|200002|200003)?$", message = "教育模式必须为200001/200002/200003")
    @Schema(description = "教育模式：200001普通 200002录播 200003直播")
    private String teachMode;

    @Size(max = 5000, message = "课程介绍不能超过5000字符")
    @Schema(description = "课程介绍")
    private String description;

    @Size(max = 500, message = "课程图片URL不能超过500字符")
    @Schema(description = "课程图片URL")
    private String pic;

    @Schema(description = "封面图fileId数组JSON，最多3个")
    private String coverPics;

    @Schema(description = "默认封面索引0-2")
    private Integer defaultCoverIndex;
}
