package com.xuecheng.content.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 分类树节点 VO（含 childrenTreeNodes）
 */
@Data
public class CourseCategoryTreeVO {

    private String id;
    private String name;
    private String label;
    private String parentId;
    private Integer isVisible;
    private Integer orderBy;
    private Integer isLeaf;

    /** 子节点列表 */
    private List<CourseCategoryTreeVO> childrenTreeNodes;
}
