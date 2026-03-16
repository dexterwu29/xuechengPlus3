package com.xuecheng.content.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("media_file")
public class MediaFile {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String fileId;
    private String md5;
    private String filePath;
    private String fileName;
    private Long fileSize;
    private String fileType;
    private Integer duration;  // 视频时长（秒），仅视频类型有效
    private String contentType;
    private String bucket;
    private Long companyId;
    private LocalDateTime createTime;
}
