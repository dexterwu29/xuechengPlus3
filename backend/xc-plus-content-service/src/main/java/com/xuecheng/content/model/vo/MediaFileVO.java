package com.xuecheng.content.model.vo;

import lombok.Data;

@Data
public class MediaFileVO {
    private String fileId;
    private String fileName;
    private Long fileSize;
    private String fileType;
    private String contentType;
    private String url;       // 预览/下载URL（预签名）
}
