package com.xuecheng.content.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 教学计划保存
 */
@Data
@Schema(description = "教学计划")
public class TeachplanDTO {

    @Schema(description = "课程ID")
    private Long courseId;
    @Schema(description = "计划名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String pname;
    @Schema(description = "父级ID，0表示章")
    private Long parentId;
    @Schema(description = "层级：1章 2节")
    private Integer grade;
    @Schema(description = "课程类型")
    private String mediaType;
    @Schema(description = "排序")
    private Integer orderBy;
    @Schema(description = "是否可试看：0否 1是")
    private String isPreviewEnabled;
}
