package com.xuecheng.content.model.vo;

import lombok.Data;

/**
 * 教学计划媒资 VO
 */
@Data
public class TeachplanMediaVO {

    private Long id;
    private String mediaId;
    private Long teachplanId;
    private Long courseId;
    private String mediaFileName;
}
