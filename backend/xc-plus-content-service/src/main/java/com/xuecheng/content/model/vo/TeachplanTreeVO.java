package com.xuecheng.content.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 教学计划树节点 VO（含 children、teachplanMedia）
 */
@Data
public class TeachplanTreeVO {

    private Long id;
    private String pname;
    private Long parentId;
    private Integer grade;
    private String mediaType;
    private Integer orderBy;
    private Long courseId;
    private Integer status;
    private String isPreviewEnabled;

    /** 子节点列表 */
    private List<TeachplanTreeVO> children;
    /** 媒资信息 */
    private TeachplanMediaVO teachplanMedia;
}
