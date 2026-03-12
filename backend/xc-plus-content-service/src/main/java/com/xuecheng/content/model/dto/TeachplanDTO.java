package com.xuecheng.content.model.dto;

import lombok.Data;

/**
 * 教学计划保存
 */
@Data
public class TeachplanDTO {

    private Long courseId;
    private String pname;
    private Long parentId;
    private Integer grade;
    private String mediaType;
    private Integer orderBy;
    private String isPreviewEnabled;
}
