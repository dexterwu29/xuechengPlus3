package com.xuecheng.content.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 视频转码配置
 * 31.4: 支持配置FFmpeg路径，用于将AVI等格式转码为MP4
 */
@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "transcode")
public class TranscodeProperties {

    /**
     * FFmpeg可执行文件路径
     * Windows示例: D:/myDataSJTU/ffmpeg/bin/ffmpeg.exe（建议用正斜杠）
     * Linux示例: /usr/bin/ffmpeg
     */
    private String ffmpegPath = "ffmpeg";

    /**
     * FFprobe可执行文件路径（用于获取视频信息）
     * 如果不配置，使用同目录下的ffprobe
     */
    private String ffprobePath;

    /**
     * 是否启用自动转码（上传AVI等不支持格式时自动转码为MP4）
     */
    private boolean autoTranscode = true;

    /**
     * 转码超时时间（秒）
     */
    private int timeoutSeconds = 600;

    @PostConstruct
    public void logConfig() {
        log.info("Transcode config: ffmpegPath={}, ffprobePath={}", ffmpegPath, ffprobePath != null ? ffprobePath : "(auto)");
    }

    /**
     * 获取FFprobe路径（与ffmpeg同目录，仅替换可执行文件名）
     */
    public String getFfprobePath() {
        if (ffprobePath != null && !ffprobePath.isBlank()) {
            return ffprobePath;
        }
        if (ffmpegPath == null || ffmpegPath.isBlank()) {
            return "ffprobe";
        }
        // 仅替换可执行文件名，避免把路径中的 ffmpeg 目录名也替换掉
        if (ffmpegPath.endsWith("ffmpeg.exe")) {
            return ffmpegPath.substring(0, ffmpegPath.length() - 10) + "ffprobe.exe";
        }
        if (ffmpegPath.endsWith("ffmpeg")) {
            return ffmpegPath.substring(0, ffmpegPath.length() - 6) + "ffprobe";
        }
        return "ffprobe";
    }

    /**
     * 检查FFmpeg是否可用
     */
    public boolean isFFmpegAvailable() {
        try {
            String path = getFfprobePath();
            ProcessBuilder pb = new ProcessBuilder(path, "-version");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.warn("ffprobe -version exited with {}", exitCode);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.warn("FFmpeg check failed: {}", e.getMessage());
            return false;
        }
    }
}
