package com.xuecheng.content.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 课程营销信息
 */
@Data
@Schema(description = "课程营销信息")
public class CourseMarketDTO {

    @Schema(description = "收费规则：201000免费 201001收费")
    private String charge;
    @Schema(description = "现价")
    private Float price;
    @Schema(description = "原价")
    private Float originalPrice;
    @Schema(description = "咨询QQ")
    private String qq;
    @Schema(description = "微信")
    private String wechat;
    @Schema(description = "电话")
    private String phone;
    @Schema(description = "有效期天数")
    private Integer validDays;
}
