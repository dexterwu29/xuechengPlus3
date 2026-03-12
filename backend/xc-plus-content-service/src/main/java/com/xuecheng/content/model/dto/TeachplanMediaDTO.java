package com.xuecheng.content.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 媒资关联
 */
@Data
@Schema(description = "计划-媒资关联")
public class TeachplanMediaDTO {

    @NotBlank(message = "媒资ID不能为空")
    @Size(max = 32, message = "媒资ID不能超过32字符")
    @Schema(description = "媒资ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String mediaId;

    @Size(max = 150, message = "媒资文件名不能超过150字符")
    @Schema(description = "媒资文件名")
    private String mediaFileName;
}
