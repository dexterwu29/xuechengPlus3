package com.xuecheng.content.controller;

import com.xuecheng.content.common.RestResponse;
import com.xuecheng.content.exception.BusinessException;
import com.xuecheng.content.model.dto.MediaAccessResult;
import com.xuecheng.content.model.vo.MediaFileVO;
import com.xuecheng.content.service.MediaAccessService;
import com.xuecheng.content.service.MediaUploadService;
import com.xuecheng.content.service.VideoTranscodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.xuecheng.content.security.LoginUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;
import java.util.Map;

/**
 * 媒资上传与查询接口
 */
@Tag(name = "媒资管理")
@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaUploadService mediaUploadService;
    private final MediaAccessService mediaAccessService;
    private final VideoTranscodeService videoTranscodeService;

    @Operation(summary = "检查MD5是否已存在")
    @PostMapping("/upload/check")
    public Map<String, Object> checkByMd5(@RequestParam String md5) {
        String fileId = mediaUploadService.checkByMd5(md5);
        return Map.of("exists", fileId != null, "fileId", fileId != null ? fileId : "");
    }

    @Operation(summary = "小文件直传（<5MB）")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> uploadSimple(@RequestParam("file") MultipartFile file) {
        String fileId = mediaUploadService.uploadSimple(file);
        return Map.of("fileId", fileId);
    }

    @Operation(summary = "初始化分片上传")
    @PostMapping("/upload/init")
    public Map<String, Object> initChunkUpload(
            @RequestParam String fileMd5,
            @RequestParam String fileName,
            @RequestParam long fileSize,
            @RequestParam(required = false, defaultValue = "0") int chunkSize) {
        var result = mediaUploadService.initChunkUpload(fileMd5, fileName, fileSize, chunkSize);
        if (result.isInstant()) {
            return Map.of("instant", true, "fileId", result.getFileId());
        }
        return Map.of("uploadId", result.getUploadId());
    }

    @Operation(summary = "查询已上传分片")
    @GetMapping("/upload/status/{uploadId}")
    public Map<String, Object> getUploadStatus(
            @Parameter(description = "上传任务ID") @PathVariable String uploadId) {
        List<Integer> chunks = mediaUploadService.getUploadedChunks(uploadId);
        return Map.of("uploadedChunks", chunks);
    }

    @Operation(summary = "上传分片")
    @PostMapping(value = "/upload/chunk/{uploadId}/{chunkIndex}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> uploadChunk(
            @PathVariable String uploadId,
            @PathVariable int chunkIndex,
            @RequestParam("chunk") MultipartFile chunk) {
        mediaUploadService.uploadChunk(uploadId, chunkIndex, chunk);
        return Map.of("message", "ok");
    }

    @Operation(summary = "完成分片上传")
    @PostMapping("/upload/complete/{uploadId}")
    public Map<String, String> completeChunkUpload(
            @Parameter(description = "上传任务ID") @PathVariable String uploadId) {
        String fileId = mediaUploadService.completeChunkUpload(uploadId);
        return Map.of("fileId", fileId);
    }

    @Operation(summary = "获取文件信息")
    @GetMapping("/{fileId}")
    public MediaFileVO getFileInfo(@PathVariable String fileId) {
        return mediaUploadService.getFileInfo(fileId);
    }

    @Operation(summary = "31.4：获取MD等文本文件内容，用于弹窗预览")
    @GetMapping(value = "/{fileId}/content", produces = "text/plain;charset=UTF-8")
    public String getFileContent(@Parameter(description = "媒资文件ID") @PathVariable String fileId) {
        String content = mediaUploadService.getFileContentAsText(fileId);
        if (content == null) {
            throw new BusinessException(404, "文件不存在或非文本类型");
        }
        return content;
    }

    @Operation(summary = "检查媒资播放权限（含 playUrl）")
    @GetMapping("/{fileId}/play")
    public RestResponse<MediaAccessResult> checkPlayAccess(
            @Parameter(description = "媒资文件ID") @PathVariable String fileId,
            @AuthenticationPrincipal LoginUser user) {
        return RestResponse.success(mediaAccessService.checkPlayAccess(fileId, user));
    }

    // ==================== 31.4: 视频转码相关接口 ====================

    @Operation(summary = "检查文件是否需要转码（31.4）")
    @GetMapping("/{fileId}/transcode/check")
    public RestResponse<Map<String, Object>> checkTranscodeNeeded(
            @Parameter(description = "媒资文件ID") @PathVariable String fileId) {
        boolean needed = videoTranscodeService.needsTranscoding(fileId);
        boolean supported = videoTranscodeService.isTranscodingSupported();
        return RestResponse.success(Map.of(
                "needsTranscoding", needed,
                "transcodingSupported", supported
        ));
    }

    @Operation(summary = "转码视频为MP4（31.4）")
    @PostMapping("/{fileId}/transcode")
    public RestResponse<Map<String, String>> transcodeToMp4(
            @Parameter(description = "媒资文件ID") @PathVariable String fileId) {
        String newFileId = videoTranscodeService.transcodeToMp4(fileId);
        return RestResponse.success(Map.of("fileId", newFileId));
    }
}
