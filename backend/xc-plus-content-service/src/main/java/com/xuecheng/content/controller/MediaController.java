package com.xuecheng.content.controller;

import com.xuecheng.content.model.vo.MediaFileVO;
import com.xuecheng.content.service.MediaUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
        String uploadId = mediaUploadService.initChunkUpload(fileMd5, fileName, fileSize, chunkSize);
        return Map.of("uploadId", uploadId);
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
}
