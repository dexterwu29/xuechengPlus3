package com.xuecheng.content.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程列表项 VO
 */
@Data
@Schema(description = "课程列表项")
public class CourseBaseVO {

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
    @Schema(description = "创建时间（东八区）")
    private LocalDateTime createTime;
    @Schema(description = "更新时间（东八区）")
    private LocalDateTime updateTime;
    @Schema(description = "审核状态：202001未通过 202002未提交 202003已提交 202004通过")
    private String auditStatus;
    @Schema(description = "发布状态：203001未发布 203002已发布 203003下线")
    private String publishStatus;
}
