package com.xuecheng.content.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 教学计划树节点 VO（含 children、teachplanMedia）
 */
@Data
@Schema(description = "教学计划树节点")
public class TeachplanTreeVO {

    @Schema(description = "计划ID")
    private Long id;
    @Schema(description = "计划名称")
    private String pname;
    @Schema(description = "父级ID")
    private Long parentId;
    @Schema(description = "层级：1章 2节")
    private Integer grade;
    @Schema(description = "课程类型")
    private String mediaType;
    @Schema(description = "排序")
    private Integer orderBy;
    @Schema(description = "课程ID")
    private Long courseId;
    @Schema(description = "计划状态")
    private Integer status;
    @Schema(description = "是否可试看")
    private String isPreviewEnabled;

    @Schema(description = "子节点列表")
    private List<TeachplanTreeVO> children;
    @Schema(description = "媒资信息（单条，兼容旧版）")
    private TeachplanMediaVO teachplanMedia;
    @Schema(description = "媒资列表（一节多文件）")
    private List<TeachplanMediaVO> mediaList;
}
