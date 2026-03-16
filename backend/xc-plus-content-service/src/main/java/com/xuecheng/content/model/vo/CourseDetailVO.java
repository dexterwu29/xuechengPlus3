package com.xuecheng.content.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 课程详情 VO（含 base + market）
 */
@Data
@Schema(description = "课程详情（含营销信息）")
public class CourseDetailVO {

    @Schema(description = "课程ID")
    private Long id;
    @Schema(description = "机构ID")
    private Long companyId;
    @Schema(description = "机构名称")
    private String companyName;
    @Schema(description = "课程名称")
    private String name;
    @Schema(description = "适用人群")
    private String users;
    @Schema(description = "课程标签")
    private String tags;
    @Schema(description = "大分类ID")
    private String mt;
    @Schema(description = "小分类ID")
    private String st;
    @Schema(description = "课程等级")
    private String grade;
    @Schema(description = "教育模式")
    private String teachMode;
    @Schema(description = "课程介绍")
    private String description;
    @Schema(description = "课程图片")
    private String pic;
    @Schema(description = "封面图fileId数组JSON，最多3个")
    private String coverPics;
    @Schema(description = "默认封面索引0-2")
    private Integer defaultCoverIndex;
    @Schema(description = "封面图URL数组（公开接口返回，供前端直接展示）")
    private List<String> coverPicUrls;
    @Schema(description = "创建时间（东八区）")
    private LocalDateTime createTime;
    @Schema(description = "更新时间（东八区）")
    private LocalDateTime updateTime;
    @Schema(description = "课程状态：000草稿 100待审核 111已发布 112已下架")
    private String courseStatus;

    @Schema(description = "收费规则")
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
