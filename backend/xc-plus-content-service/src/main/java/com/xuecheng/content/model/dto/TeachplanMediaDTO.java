package com.xuecheng.content.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 媒资关联
 */
@Data
@Schema(description = "计划-媒资关联")
public class TeachplanMediaDTO {

    @Schema(description = "媒资ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String mediaId;
    @Schema(description = "媒资文件名")
    private String mediaFileName;
}
