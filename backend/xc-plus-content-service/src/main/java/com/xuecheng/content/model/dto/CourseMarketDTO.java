package com.xuecheng.content.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 课程营销信息
 */
@Data
@Schema(description = "课程营销信息")
public class CourseMarketDTO {

    @Pattern(regexp = "^(201000|201001)?$", message = "收费规则必须为201000免费或201001收费")
    @Schema(description = "收费规则：201000免费 201001收费")
    private String charge;

    @DecimalMin(value = "0", message = "现价不能为负数")
    @Schema(description = "现价")
    private Float price;

    @DecimalMin(value = "0", message = "原价不能为负数")
    @Schema(description = "原价")
    private Float originalPrice;

    @Size(max = 32, message = "咨询QQ不能超过32字符")
    @Schema(description = "咨询QQ")
    private String qq;

    @Size(max = 64, message = "微信不能超过64字符")
    @Schema(description = "微信")
    private String wechat;

    @Size(max = 32, message = "电话不能超过32字符")
    @Schema(description = "电话")
    private String phone;

    @Min(value = 0, message = "有效期天数不能为负数")
    @Max(value = 9999, message = "有效期天数不能超过9999")
    @Schema(description = "有效期天数")
    private Integer validDays;
}
