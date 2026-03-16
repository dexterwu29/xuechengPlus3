package com.xuecheng.content.service;

/**
 * 视频转码服务
 * 31.4: 用于将非浏览器支持的视频格式（如AVI）转码为MP4
 */
public interface VideoTranscodeService {

    /**
     * 触发视频文件转码
     *
     * @param fileId     原视频文件ID
     * @return 转码后的新文件ID（MP4格式），转码失败返回null
     */
    String transcodeToMp4(String fileId);

    /**
     * 检查文件是否需要转码
     *
     * @param fileId 文件ID
     * @return 是否需要转码
     */
    boolean needsTranscoding(String fileId);

    /**
     * 检查服务器是否支持转码（FFmpeg是否可用）
     *
     * @return 是否支持转码
     */
    boolean isTranscodingSupported();
}
