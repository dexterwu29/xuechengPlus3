package com.xuecheng.content.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    private String endpoint = "http://localhost:9000";
    private String accessKey = "minioadmin";
    private String secretKey = "minioadmin";
    private String bucket = "xcplus";
    private int chunkSize = 5 * 1024 * 1024; // 5MB

    /**
     * 方案A：强制双桶。视频 -> bucket-video，文档/图片/其他 -> bucket-doc
     * 桶名固定派生：{bucket}-video、{bucket}-doc，不可配置切换
     */
    public String getBucketVideo() {
        return bucket + "-video";
    }

    public String getBucketDoc() {
        return bucket + "-doc";
    }

    /** 根据文件类型选择目标桶 */
    public String getBucketForFileType(String fileType) {
        if (fileType != null && "video".equalsIgnoreCase(fileType)) {
            return getBucketVideo();
        }
        return getBucketDoc();
    }
}
