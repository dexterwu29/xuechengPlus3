package com.xuecheng.content.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审核动作
 */
@Getter
@AllArgsConstructor
public enum AuditAction {

    SUBMIT("submit", "提交审核"),
    APPROVE("approve", "审核通过"),
    REJECT("reject", "拒绝退回"),
    BAN("ban", "封禁下架"),
    OFFLINE("offline", "下线");

    private final String code;
    private final String desc;
}
