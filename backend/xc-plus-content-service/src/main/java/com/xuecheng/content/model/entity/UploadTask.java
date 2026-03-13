package com.xuecheng.content.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("upload_task")
public class UploadTask {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String uploadId;
    private String fileMd5;
    private String fileName;
    private Long fileSize;
    private Integer chunkSize;
    private Integer chunkCount;
    private Integer status;
    private Long companyId;
    private LocalDateTime createTime;
}
