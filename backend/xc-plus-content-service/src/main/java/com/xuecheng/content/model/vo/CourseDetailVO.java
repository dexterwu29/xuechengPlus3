package com.xuecheng.content.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程详情 VO（含 base + market）
 */
@Data
public class CourseDetailVO {

    private Long id;
    private Long companyId;
    private String companyName;
    private String name;
    private String users;
    private String tags;
    private String mt;
    private String st;
    private String grade;
    private String teachMode;
    private String description;
    private String pic;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String auditStatus;
    private String status;

    /** 营销信息 */
    private String charge;
    private Float price;
    private Float originalPrice;
    private String qq;
    private String wechat;
    private String phone;
    private Integer validDays;
}
