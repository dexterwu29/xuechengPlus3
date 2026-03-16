package com.xuecheng.content.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 站内信类型
 */
@Getter
@AllArgsConstructor
public enum MessageType {

    COURSE_SUBMIT("COURSE_SUBMIT", "课程提交审核"),
    AUDIT_APPROVED("AUDIT_APPROVED", "审核通过"),
    AUDIT_REJECTED("AUDIT_REJECTED", "审核退回"),
    AUDIT_BANNED("AUDIT_BANNED", "审核封禁");

    private final String code;
    private final String desc;
}
