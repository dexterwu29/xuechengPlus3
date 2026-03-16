package com.xuecheng.content.util;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 视频时长解析工具类
 * 使用 FFprobe 或 FFmpeg 解析视频文件获取时长
 * 31.4: 新增 AVI 转 MP4 转码功能，支持自定义FFmpeg路径
 */
@Slf4j
public class VideoDurationUtil {

    private static final Pattern DURATION_PATTERN = Pattern.compile("Duration: (\\d{2}):(\\d{2}):(\\d{2})\\.\\d{2}");

    /**
     * 不需要浏览器转码的视频格式
     */
    private static final String[] BROWSER_SUPPORTED_FORMATS = {"mp4", "webm", "ogg"};

    /**
     * 从视频URL解析时长（秒）
     * 注意：此方法需要服务器安装 FFprobe 或 FFmpeg
     *
     * @param videoUrl 视频 URL（需要是可访问的 HTTP URL）
     * @return 时长（秒），解析失败返回 null
     */
    public static Integer parseDuration(String videoUrl) {
        return parseDuration(videoUrl, "ffprobe", "ffmpeg");
    }

    /**
     * 从视频URL解析时长（秒），支持自定义FFmpeg路径
     */
    public static Integer parseDuration(String videoUrl, String ffprobePath, String ffmpegPath) {
        if (videoUrl == null || videoUrl.isBlank()) {
            return null;
        }

        // 首先尝试 FFprobe
        Integer duration = parseWithFFprobe(videoUrl, ffprobePath);
        if (duration != null) {
            return duration;
        }

        // 其次尝试 FFmpeg
        return parseWithFFmpeg(videoUrl, ffmpegPath);
    }

    /**
     * 使用 FFprobe 解析时长
     */
    private static Integer parseWithFFprobe(String url, String ffprobePath) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                ffprobePath,
                "-v", "error",
                "-show_entries", "format=duration",
                "-of", "default=noprint_wrappers=1:nokey=1",
                url
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            process.waitFor();

            if (line != null && !line.isBlank()) {
                double d = Double.parseDouble(line.trim());
                return (int) Math.round(d);
            }
        } catch (Exception e) {
            log.debug("FFprobe not available or failed: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 使用 FFmpeg 解析时长（备用方案）
     */
    private static Integer parseWithFFmpeg(String url, String ffmpegPath) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                ffmpegPath,
                "-i", url,
                "-f", "null",
                "-"
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher m = DURATION_PATTERN.matcher(line);
                if (m.find()) {
                    int hours = Integer.parseInt(m.group(1));
                    int minutes = Integer.parseInt(m.group(2));
                    int seconds = Integer.parseInt(m.group(3));
                    return hours * 3600 + minutes * 60 + seconds;
                }
            }
            process.waitFor();
        } catch (Exception e) {
            log.debug("FFmpeg not available or failed: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 检查文件是否为视频（根据扩展名）
     */
    public static boolean isVideoFile(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return false;
        }
        String ext = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        return java.util.List.of("mp4", "avi", "mov", "mkv", "webm", "flv", "wmv").contains(ext);
    }

    /**
     * 检查视频格式是否需要转码（非浏览器原生支持的格式）
     * 31.4
     */
    public static boolean needsTranscoding(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return false;
        }
        String ext = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        for (String supported : BROWSER_SUPPORTED_FORMATS) {
            if (supported.equals(ext)) {
                return false;
            }
        }
        return isVideoFile(fileName);
    }

    /**
     * 获取文件扩展名
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    /**
     * 转换输出文件名为MP4格式
     */
    public static String getMp4FileName(String originalFileName) {
        if (originalFileName == null || !originalFileName.contains(".")) {
            return originalFileName + ".mp4";
        }
        int lastDot = originalFileName.lastIndexOf('.');
        return originalFileName.substring(0, lastDot) + ".mp4";
    }

    /**
     * 31.4: 使用 FFmpeg 将视频文件转码为 MP4（使用默认路径）
     */
    public static boolean transcodeToMp4(String inputPath, String outputPath) {
        return transcodeToMp4(inputPath, outputPath, "ffmpeg");
    }

    /**
     * 31.4: 使用 FFmpeg 将视频文件转码为 MP4（支持自定义FFmpeg路径）
     *
     * @param inputPath  输入文件路径（本地文件系统）
     * @param outputPath 输出文件路径（本地文件系统）
     * @param ffmpegPath FFmpeg可执行文件路径
     * @return 转码是否成功
     */
    public static boolean transcodeToMp4(String inputPath, String outputPath, String ffmpegPath) {
        if (inputPath == null || inputPath.isBlank() || outputPath == null || outputPath.isBlank()) {
            log.error("transcodeToMp4: invalid paths");
            return false;
        }

        File inputFile = new File(inputPath);
        if (!inputFile.exists()) {
            log.error("transcodeToMp4: input file does not exist: {}", inputPath);
            return false;
        }

        try {
            // 创建输出目录
            File outputFile = new File(outputPath);
            File outputDir = outputFile.getParentFile();
            if (outputDir != null && !outputDir.exists()) {
                outputDir.mkdirs();
            }

            // FFmpeg 转码命令：
            // -i: 输入文件
            // -c:v libx264: 视频编码器
            // -preset fast: 转码速度平衡
            // -crf 23: 视频质量（值越小质量越高）
            // -c:a aac: 音频编码器
            // -b:a 128k: 音频比特率
            // -movflags +faststart: 优化MP4用于网络流媒体
            ProcessBuilder pb = new ProcessBuilder(
                ffmpegPath,
                "-i", inputPath,
                "-c:v", "libx264",
                "-preset", "fast",
                "-crf", "23",
                "-c:a", "aac",
                "-b:a", "128k",
                "-movflags", "+faststart",
                "-y", // 覆盖输出文件
                outputPath
            );

            pb.redirectErrorStream(true);
            log.info("Starting transcoding: {} -> {}", inputPath, outputPath);

            Process process = pb.start();

            // 读取输出日志
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                log.debug("ffmpeg: {}", line);
            }

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                log.info("Transcoding completed successfully: {}", outputPath);
                return true;
            } else {
                log.error("Transcoding failed with exit code: {}", exitCode);
                return false;
            }

        } catch (Exception e) {
            log.error("Transcoding failed", e);
            return false;
        }
    }

    /**
     * 31.4: 检查 FFmpeg 是否可用（使用默认路径）
     */
    public static boolean isFFmpegAvailable() {
        return isFFmpegAvailable("ffmpeg");
    }

    /**
     * 31.4: 检查 FFmpeg 是否可用（支持自定义路径）
     */
    public static boolean isFFmpegAvailable(String ffmpegPath) {
        try {
            ProcessBuilder pb = new ProcessBuilder(ffmpegPath, "-version");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            log.debug("FFmpeg not available: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 31.4: 获取转码后的文件大小（预估）
     * 基于原文件大小估算，MP4 (H.264) 通常比 AVI (XVID/DivX) 小 20-50%
     */
    public static long estimateTranscodedSize(long originalSize) {
        // 保守估计：70% 原大小
        return (long) (originalSize * 0.7);
    }
}
