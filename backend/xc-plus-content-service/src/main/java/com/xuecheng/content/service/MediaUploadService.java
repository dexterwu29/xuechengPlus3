package com.xuecheng.content.service;

import com.xuecheng.content.model.vo.MediaFileVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 媒资上传服务 - MD5去重、断点续传
 */
public interface MediaUploadService {

    /**
     * 检查文件是否已存在（按MD5）
     * @return 若存在返回 fileId，否则返回 null
     */
    String checkByMd5(String md5);

    /**
     * 小文件直传（<5MB），支持MD5去重
     */
    String uploadSimple(MultipartFile file);

    /**
     * 初始化分片上传
     * @param fileMd5 文件MD5
     * @param fileName 文件名
     * @param fileSize 文件大小
     * @param chunkSize 分片大小
     * @return uploadId
     */
    String initChunkUpload(String fileMd5, String fileName, long fileSize, int chunkSize);

    /**
     * 查询已上传的分片索引列表
     */
    List<Integer> getUploadedChunks(String uploadId);

    /**
     * 上传单个分片
     */
    void uploadChunk(String uploadId, int chunkIndex, MultipartFile chunk);

    /**
     * 合并分片并完成上传
     * @return fileId
     */
    String completeChunkUpload(String uploadId);

    /**
     * 获取文件信息（含预览URL）
     */
    MediaFileVO getFileInfo(String fileId);
}
