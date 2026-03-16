package com.xuecheng.content.service;

import com.xuecheng.content.model.vo.InitChunkResult;
import com.xuecheng.content.model.vo.MediaFileVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 媒资上传服务 - MD5去重、断点续传、秒传
 */
public interface MediaUploadService {

    /**
     * 检查文件是否已存在（按MD5，且 MinIO 有对应文件）
     * @return 若存在返回 fileId，否则返回 null
     */
    String checkByMd5(String md5);

    /**
     * 小文件直传（<5MB），支持MD5秒传
     */
    String uploadSimple(MultipartFile file);

    /**
     * 初始化分片上传；若文件已存在（秒传）则返回 fileId
     */
    InitChunkResult initChunkUpload(String fileMd5, String fileName, long fileSize, int chunkSize);

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

    /**
     * 31.4：获取文本类文件内容（如 .md），用于弹窗预览，避免 CORS
     * @return 文件内容文本，非文本文件返回 null
     */
    String getFileContentAsText(String fileId);
}
