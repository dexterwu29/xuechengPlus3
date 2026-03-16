package com.xuecheng.content.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 审核请求体
 */
@Data
@Schema(description = "审核请求")
public class AuditDTO {

    @NotBlank(message = "审核动作不能为空")
    @Pattern(regexp = "^(approve|reject|ban)$", message = "审核动作必须为approve/reject/ban")
    @Schema(description = "审核动作：approve通过 reject退回 ban封禁", requiredMode = Schema.RequiredMode.REQUIRED)
    private String action;

    @Schema(description = "审核意见/退回原因")
    private String opinion;
}
