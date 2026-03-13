package com.xuecheng.content.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("upload_chunk")
public class UploadChunk {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String uploadId;
    private Integer chunkIndex;
    private String chunkPath;
    private LocalDateTime createTime;
}
