package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.content.config.CompanyContext;
import com.xuecheng.content.config.MinioProperties;
import com.xuecheng.content.exception.BusinessException;
import com.xuecheng.content.model.entity.MediaFile;
import com.xuecheng.content.model.entity.UploadChunk;
import com.xuecheng.content.model.entity.UploadTask;
import com.xuecheng.content.model.vo.InitChunkResult;
import com.xuecheng.content.model.vo.MediaFileVO;
import com.xuecheng.content.service.MediaUploadService;
import com.xuecheng.content.mapper.MediaFileMapper;
import com.xuecheng.content.mapper.UploadChunkMapper;
import com.xuecheng.content.mapper.UploadTaskMapper;
import io.minio.*;
import io.minio.ComposeSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 媒资上传服务实现 - MD5去重、分片断点续传
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MediaUploadServiceImpl implements MediaUploadService {

    private static final int SIMPLE_UPLOAD_THRESHOLD = 5 * 1024 * 1024; // 5MB
    private static final int TASK_STATUS_PENDING = 0;
    private static final int TASK_STATUS_COMPLETED = 1;

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final MediaFileMapper mediaFileMapper;
    private final UploadTaskMapper uploadTaskMapper;
    private final UploadChunkMapper uploadChunkMapper;

    @Override
    public String checkByMd5(String md5) {
        if (md5 == null || md5.isBlank()) return null;
        String m = md5.trim().toLowerCase();
        LambdaQueryWrapper<MediaFile> q = new LambdaQueryWrapper<>();
        q.eq(MediaFile::getMd5, m).last("LIMIT 1");
        MediaFile f = mediaFileMapper.selectOne(q);
        if (f == null) return null;
        if (!minioObjectExists(f.getBucket(), f.getFilePath())) return null;
        return f.getFileId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadSimple(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "文件不能为空");
        }
        if (file.getSize() > SIMPLE_UPLOAD_THRESHOLD) {
            throw new BusinessException(400, "文件超过5MB，请使用分片上传");
        }
        Long companyId = CompanyContext.getCompanyId();
        if (companyId == null) {
            throw new BusinessException(403, "缺少机构标识");
        }

        try {
            byte[] bytes = file.getBytes();
            String md5 = md5Hex(bytes);
            String exist = checkByMd5(md5);
            if (exist != null) {
                return exist;
            }

            String fileId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            String ext = getExtension(file.getOriginalFilename());
            String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown";
            String objectPath = buildMd5ObjectPath(md5, fileName);
            String fileType = inferFileType(file.getOriginalFilename(), file.getContentType());
            String bucket = minioProperties.getBucketForFileType(fileType);
            String contentType = file.getContentType();
            if (contentType == null || contentType.isBlank()) {
                contentType = "application/octet-stream";
            }

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectPath)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(contentType)
                            .build());

            // 31.4: 检查MySQL中是否已有该MD5记录（MinIO文件可能已删除）
            MediaFile existingFile = mediaFileMapper.selectOne(
                    new LambdaQueryWrapper<MediaFile>().eq(MediaFile::getMd5, md5));

            if (existingFile != null) {
                // MySQL有记录但MinIO文件缺失，更新记录
                existingFile.setFilePath(objectPath);
                existingFile.setFileName(fileName);
                existingFile.setFileSize(file.getSize());
                existingFile.setFileType(fileType);
                existingFile.setContentType(contentType);
                existingFile.setBucket(bucket);
                existingFile.setCreateTime(LocalDateTime.now());
                mediaFileMapper.updateById(existingFile);
                return existingFile.getFileId();
            }

            MediaFile mf = new MediaFile();
            mf.setFileId(fileId);
            mf.setMd5(md5);
            mf.setFilePath(objectPath);
            mf.setFileName(fileName);
            mf.setFileSize(file.getSize());
            mf.setFileType(fileType);
            mf.setContentType(contentType);
            mf.setBucket(bucket);
            mf.setCompanyId(companyId);
            mf.setCreateTime(LocalDateTime.now());
            mediaFileMapper.insert(mf);
            return fileId;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("uploadSimple failed", e);
            throw new BusinessException(500, "上传失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InitChunkResult initChunkUpload(String fileMd5, String fileName, long fileSize, int chunkSize) {
        if (fileMd5 == null || fileMd5.isBlank() || fileName == null || fileName.isBlank()) {
            throw new BusinessException(400, "fileMd5 和 fileName 不能为空");
        }
        Long companyId = CompanyContext.getCompanyId();
        if (companyId == null) {
            throw new BusinessException(403, "缺少机构标识");
        }

        String m = fileMd5.trim().toLowerCase();
        String exist = checkByMd5(m);
        if (exist != null) {
            return InitChunkResult.instant(exist);
        }

        int cs = chunkSize > 0 ? chunkSize : minioProperties.getChunkSize();
        int chunkCount = (int) ((fileSize + cs - 1) / cs);
        String uploadId = UUID.randomUUID().toString().replace("-", "");

        UploadTask task = new UploadTask();
        task.setUploadId(uploadId);
        task.setFileMd5(m);
        task.setFileName(fileName);
        task.setFileSize(fileSize);
        task.setChunkSize(cs);
        task.setChunkCount(chunkCount);
        task.setStatus(TASK_STATUS_PENDING);
        task.setCompanyId(companyId);
        task.setCreateTime(LocalDateTime.now());
        uploadTaskMapper.insert(task);
        return InitChunkResult.normal(uploadId);
    }

    @Override
    public List<Integer> getUploadedChunks(String uploadId) {
        LambdaQueryWrapper<UploadChunk> q = new LambdaQueryWrapper<>();
        q.eq(UploadChunk::getUploadId, uploadId).orderByAsc(UploadChunk::getChunkIndex);
        return uploadChunkMapper.selectList(q).stream()
                .map(UploadChunk::getChunkIndex)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadChunk(String uploadId, int chunkIndex, MultipartFile chunk) {
        UploadTask task = uploadTaskMapper.selectOne(
                new LambdaQueryWrapper<UploadTask>().eq(UploadTask::getUploadId, uploadId));
        if (task == null) {
            throw new BusinessException(404, "上传任务不存在");
        }
        if (task.getStatus() == TASK_STATUS_COMPLETED) {
            throw new BusinessException(400, "任务已完成");
        }
        if (chunkIndex < 0 || chunkIndex >= task.getChunkCount()) {
            throw new BusinessException(400, "分片索引无效");
        }

        String chunkPath = "chunk/" + uploadId + "/" + chunkIndex;
        String fileType = inferFileType(task.getFileName(), null);
        String bucket = minioProperties.getBucketForFileType(fileType);

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(chunkPath)
                            .stream(chunk.getInputStream(), chunk.getSize(), -1)
                            .contentType("application/octet-stream")
                            .build());

            UploadChunk uc = new UploadChunk();
            uc.setUploadId(uploadId);
            uc.setChunkIndex(chunkIndex);
            uc.setChunkPath(chunkPath);
            uc.setCreateTime(LocalDateTime.now());
            uploadChunkMapper.insert(uc);
        } catch (Exception e) {
            log.error("uploadChunk failed uploadId={} chunkIndex={}", uploadId, chunkIndex, e);
            throw new BusinessException(500, "分片上传失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String completeChunkUpload(String uploadId) {
        UploadTask task = uploadTaskMapper.selectOne(
                new LambdaQueryWrapper<UploadTask>().eq(UploadTask::getUploadId, uploadId));
        if (task == null) {
            throw new BusinessException(404, "上传任务不存在");
        }
        if (task.getStatus() == TASK_STATUS_COMPLETED) {
            MediaFile mf = mediaFileMapper.selectOne(
                    new LambdaQueryWrapper<MediaFile>().eq(MediaFile::getMd5, task.getFileMd5()));
            return mf != null ? mf.getFileId() : null;
        }

        List<UploadChunk> chunks = uploadChunkMapper.selectList(
                new LambdaQueryWrapper<UploadChunk>()
                        .eq(UploadChunk::getUploadId, uploadId)
                        .orderByAsc(UploadChunk::getChunkIndex));
        if (chunks.size() != task.getChunkCount()) {
            throw new BusinessException(400, "分片未上传完整，已上传 " + chunks.size() + "/" + task.getChunkCount());
        }

        String fileType = inferFileType(task.getFileName(), null);
        String bucket = minioProperties.getBucketForFileType(fileType);
        String fileId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String objectPath = buildMd5ObjectPath(task.getFileMd5(), task.getFileName());

        try {
            List<ComposeSource> sources = chunks.stream()
                    .map(c -> ComposeSource.builder().bucket(bucket).object(c.getChunkPath()).build())
                    .collect(Collectors.toList());

            minioClient.composeObject(
                    ComposeObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectPath)
                            .sources(sources)
                            .build());

            for (UploadChunk c : chunks) {
                minioClient.removeObject(
                        RemoveObjectArgs.builder().bucket(bucket).object(c.getChunkPath()).build());
            }

            String contentType = inferContentType(task.getFileName());

            // 31.4: 检查MySQL中是否已有该MD5记录（MinIO文件可能已删除）
            MediaFile existingFile = mediaFileMapper.selectOne(
                    new LambdaQueryWrapper<MediaFile>().eq(MediaFile::getMd5, task.getFileMd5()));

            if (existingFile != null) {
                // MySQL有记录但MinIO文件缺失，更新记录
                existingFile.setFilePath(objectPath);
                existingFile.setFileName(task.getFileName());
                existingFile.setFileSize(task.getFileSize());
                existingFile.setFileType(inferFileType(task.getFileName(), contentType));
                existingFile.setContentType(contentType);
                existingFile.setBucket(bucket);
                existingFile.setCreateTime(LocalDateTime.now());
                mediaFileMapper.updateById(existingFile);

                task.setStatus(TASK_STATUS_COMPLETED);
                uploadTaskMapper.updateById(task);
                return existingFile.getFileId();
            }

            MediaFile mf = new MediaFile();
            mf.setFileId(fileId);
            mf.setMd5(task.getFileMd5());
            mf.setFilePath(objectPath);
            mf.setFileName(task.getFileName());
            mf.setFileSize(task.getFileSize());
            mf.setFileType(inferFileType(task.getFileName(), contentType));
            mf.setContentType(contentType);
            mf.setBucket(bucket);
            mf.setCompanyId(task.getCompanyId());
            mf.setCreateTime(LocalDateTime.now());
            mediaFileMapper.insert(mf);

            task.setStatus(TASK_STATUS_COMPLETED);
            uploadTaskMapper.updateById(task);
            return fileId;
        } catch (Exception e) {
            log.error("completeChunkUpload failed uploadId={}", uploadId, e);
            throw new BusinessException(500, "合并失败: " + e.getMessage());
        }
    }

    @Override
    public MediaFileVO getFileInfo(String fileId) {
        MediaFile mf = mediaFileMapper.selectOne(
                new LambdaQueryWrapper<MediaFile>().eq(MediaFile::getFileId, fileId));
        if (mf == null) {
            throw new BusinessException(404, "文件不存在");
        }
        MediaFileVO vo = new MediaFileVO();
        vo.setFileId(mf.getFileId());
        vo.setFileName(mf.getFileName());
        vo.setFileSize(mf.getFileSize());
        vo.setFileType(mf.getFileType());
        vo.setContentType(mf.getContentType());
        try {
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(io.minio.http.Method.GET)
                            .bucket(mf.getBucket())
                            .object(mf.getFilePath())
                            .expiry(60 * 60) // 1h
                            .build());
            vo.setUrl(url);
        } catch (Exception e) {
            log.warn("getPresignedObjectUrl failed fileId={}", fileId, e);
        }
        return vo;
    }

    private String md5Hex(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(bytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new BusinessException(500, "MD5计算失败");
        }
    }

    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) return "";
        return "." + fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    private String inferFileType(String fileName, String contentType) {
        if (fileName != null) {
            String ext = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
            if (Arrays.asList("mp4", "avi", "mov", "mkv", "webm").contains(ext)) return "video";
            if (Arrays.asList("zip", "rar", "7z").contains(ext)) return "zip";
            if (Arrays.asList("jpg", "jpeg", "png", "gif", "webp").contains(ext)) return "image";
        }
        if (contentType != null) {
            if (contentType.contains("video")) return "video";
            if (contentType.contains("image")) return "image";
        }
        return "doc";
    }

    /**
     * MD5 分层路径：{md5[0]}/{md5[1]}/{md5}/{md5}_{baseName}.{ext}
     */
    private String buildMd5ObjectPath(String md5, String fileName) {
        if (md5 == null || md5.length() < 2) {
            md5 = "00";
        }
        String ext = getExtension(fileName);
        String baseName = fileName != null && fileName.contains(".")
                ? fileName.substring(0, fileName.lastIndexOf('.'))
                : "file";
        baseName = baseName.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5_-]", "_");
        if (baseName.length() > 64) baseName = baseName.substring(0, 64);
        return String.format("%s/%s/%s/%s_%s%s",
                md5.substring(0, 1), md5.substring(1, 2), md5, md5, baseName, ext);
    }

    private boolean minioObjectExists(String bucket, String objectPath) {
        try {
            minioClient.statObject(
                    io.minio.StatObjectArgs.builder().bucket(bucket).object(objectPath).build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String inferContentType(String fileName) {
        if (fileName == null) return "application/octet-stream";
        String ext = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        return switch (ext) {
            case "pdf" -> "application/pdf";
            case "doc", "docx" -> "application/msword";
            case "ppt", "pptx" -> "application/vnd.ms-powerpoint";
            case "md" -> "text/markdown";
            case "mp4" -> "video/mp4";
            case "zip" -> "application/zip";
            default -> "application/octet-stream";
        };
    }

    @Override
    public String getFileContentAsText(String fileId) {
        MediaFile mf = mediaFileMapper.selectOne(
                new LambdaQueryWrapper<MediaFile>().eq(MediaFile::getFileId, fileId));
        if (mf == null) return null;
        String fn = mf.getFileName();
        if (fn == null) return null;
        String ext = fn.substring(fn.lastIndexOf('.') + 1).toLowerCase();
        if (!"md".equals(ext) && !"markdown".equals(ext)) return null;
        try (InputStream is = minioClient.getObject(
                GetObjectArgs.builder().bucket(mf.getBucket()).object(mf.getFilePath()).build())) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("getFileContentAsText failed fileId={}", fileId, e);
            return null;
        }
    }
}
