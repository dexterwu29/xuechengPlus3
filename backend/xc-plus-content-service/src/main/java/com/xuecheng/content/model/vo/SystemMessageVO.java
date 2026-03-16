package com.xuecheng.content.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 站内信 VO
 */
@Data
@Schema(description = "站内信")
public class SystemMessageVO {

    @Schema(description = "消息ID")
    private Long id;
    @Schema(description = "消息类型")
    private String type;
    @Schema(description = "标题")
    private String title;
    @Schema(description = "内容")
    private String content;
    @Schema(description = "课程ID")
    private Long courseId;
    @Schema(description = "课程名称")
    private String courseName;
    @Schema(description = "是否已读")
    private Boolean isRead;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
