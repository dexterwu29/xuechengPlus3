package com.xuecheng.content.model.dto;

import lombok.Data;

/**
 * 课程营销信息
 */
@Data
public class CourseMarketDTO {

    private String charge;
    private Float price;
    private Float originalPrice;
    private String qq;
    private String wechat;
    private String phone;
    private Integer validDays;
}
