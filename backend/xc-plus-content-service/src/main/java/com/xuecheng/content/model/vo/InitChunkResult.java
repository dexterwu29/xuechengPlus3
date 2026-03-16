package com.xuecheng.content.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分片上传初始化结果：秒传时返回 fileId，否则返回 uploadId
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InitChunkResult {
    private String uploadId;
    private String fileId;
    private boolean instant;

    public static InitChunkResult instant(String fileId) {
        return new InitChunkResult(null, fileId, true);
    }

    public static InitChunkResult normal(String uploadId) {
        return new InitChunkResult(uploadId, null, false);
    }
}
