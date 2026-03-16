package com.xuecheng.content.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 备案创建/更新 DTO
 */
@Data
@Schema(description = "备案信息")
public class ArchiveDTO {

    @NotBlank(message = "名称不能为空")
    @Schema(description = "名称")
    private String name;

    @Schema(description = "类型：org/teacher/sales，新增时必填")
    @Pattern(regexp = "^(org|teacher|sales)?$", message = "类型必须为org/teacher/sales")
    private String archiveType;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "微信")
    private String wechat;

    @Schema(description = "QQ")
    private String qq;

    @Schema(description = "身份证号（讲师）")
    private String idCard;

    @Schema(description = "营业执照号（机构）")
    private String licenseNo;
}
