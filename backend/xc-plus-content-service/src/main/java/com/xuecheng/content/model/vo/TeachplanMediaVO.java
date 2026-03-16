package com.xuecheng.content.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 教学计划媒资 VO
 */
@Data
@Schema(description = "计划-媒资关联信息")
public class TeachplanMediaVO {

    @Schema(description = "主键")
    private Long id;
    @Schema(description = "媒资ID（兼容）")
    private String mediaId;
    @Schema(description = "media_file.file_id")
    private String fileId;
    @Schema(description = "计划ID")
    private Long teachplanId;
    @Schema(description = "课程ID")
    private Long courseId;
    @Schema(description = "媒资文件名")
    private String mediaFileName;
    @Schema(description = "媒资类型 video/doc")
    private String mediaType;
    @Schema(description = "视频时长（秒），仅视频类型有效")
    private Integer duration;
    @Schema(description = "同节内排序")
    private Integer orderBy;
}
