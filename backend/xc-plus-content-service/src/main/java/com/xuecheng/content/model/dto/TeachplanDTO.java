package com.xuecheng.content.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 教学计划保存
 */
@Data
@Schema(description = "教学计划")
public class TeachplanDTO {

    @Schema(description = "课程ID")
    private Long courseId;

    @NotBlank(message = "计划名称不能为空")
    @Size(max = 64, message = "计划名称不能超过64字符")
    @Schema(description = "计划名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String pname;

    @Schema(description = "父级ID，0表示章")
    private Long parentId;

    @NotNull(message = "层级不能为空")
    @Min(value = 1, message = "层级必须为1章或2节")
    @Max(value = 2, message = "层级必须为1章或2节")
    @Schema(description = "层级：1章 2节")
    private Integer grade;

    @Size(max = 10, message = "课程类型不能超过10字符")
    @Schema(description = "课程类型")
    private String mediaType;

    @Min(value = 0, message = "排序不能为负数")
    @Schema(description = "排序，默认0")
    private Integer orderBy;

    @Pattern(regexp = "^[01]?$", message = "是否可试看必须为0或1")
    @Schema(description = "是否可试看：0否 1是")
    private String isPreviewEnabled;
}
