package com.xuecheng.content.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 站内信
 */
@Data
@TableName("system_message")
public class SystemMessage {

    @TableId
    private Long id;
    private String type;
    private String title;
    private String content;
    private Long courseId;
    private String courseName;
    private Long fromUserId;
    private String fromUserName;
    private Long toUserId;
    private String toRole;
    private Integer isRead;
    private LocalDateTime createTime;
    private LocalDateTime readTime;
}
