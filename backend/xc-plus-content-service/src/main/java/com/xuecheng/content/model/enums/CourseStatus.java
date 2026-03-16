package com.xuecheng.content.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 课程状态（三位码）
 * 000草稿 / 100待审核 / 111已发布 / 112已下架
 */
@Getter
@AllArgsConstructor
public enum CourseStatus {

    DRAFT("000", "草稿"),
    PENDING("100", "待审核"),
    PUBLISHED("111", "已发布"),
    BANNED("112", "已下架");

    private final String code;
    private final String desc;

    public static boolean isValid(String code) {
        if (code == null || code.isBlank()) return false;
        for (CourseStatus s : values()) {
            if (s.getCode().equals(code)) return true;
        }
        return false;
    }
}
