package com.xuecheng.content.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 媒资播放权限检查结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MediaAccessResult {
    private boolean allowed;
    private String reason;   // not_logged_in | not_purchased | course_not_published
    private String playUrl;  // 当 allowed=true 时返回

    public static MediaAccessResult deny(String reason) {
        return new MediaAccessResult(false, reason, null);
    }

    public static MediaAccessResult allow(String playUrl) {
        return new MediaAccessResult(true, null, playUrl);
    }
}
