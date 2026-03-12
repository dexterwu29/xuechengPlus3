package com.xuecheng.content.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 分类树节点 VO（含 childrenTreeNodes）
 */
@Data
@Schema(description = "课程分类树节点")
public class CourseCategoryTreeVO {

    @Schema(description = "分类ID")
    private String id;
    @Schema(description = "分类名称")
    private String name;
    @Schema(description = "分类标签")
    private String label;
    @Schema(description = "父节点ID")
    private String parentId;
    @Schema(description = "是否可见：0否 1是")
    private Integer isVisible;
    @Schema(description = "排序")
    private Integer orderBy;
    @Schema(description = "是否为叶子节点：0否 1是")
    private Integer isLeaf;

    @Schema(description = "子节点列表")
    private List<CourseCategoryTreeVO> childrenTreeNodes;
}
