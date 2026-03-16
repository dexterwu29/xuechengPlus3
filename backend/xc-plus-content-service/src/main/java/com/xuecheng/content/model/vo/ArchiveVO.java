package com.xuecheng.content.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 备案 VO
 */
@Data
@Schema(description = "备案信息")
public class ArchiveVO {

    @Schema(description = "ID")
    private Long id;
    @Schema(description = "机构ID")
    private Long companyId;
    @Schema(description = "类型：org/teacher/sales")
    private String archiveType;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "联系电话")
    private String phone;
    @Schema(description = "微信")
    private String wechat;
    @Schema(description = "QQ")
    private String qq;
    @Schema(description = "身份证号")
    private String idCard;
    @Schema(description = "营业执照号")
    private String licenseNo;
    @Schema(description = "状态：pending/approved/rejected")
    private String status;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
