package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.TeachplanMediaDTO;
import com.xuecheng.content.model.vo.TeachplanMediaVO;

import java.util.List;

/**
 * 课程计划媒资服务
 */
public interface TeachplanMediaService {

    void bind(Long teachplanId, TeachplanMediaDTO dto);

    void unbind(Long teachplanId);

    void unbindMedia(Long teachplanId, String fileId);

    List<TeachplanMediaVO> listMedia(Long teachplanId);

    /**
     * 31.4：更新视频时长
     * @param teachplanId 教学计划ID
     * @param fileId 媒资文件ID
     * @param duration 视频时长（秒）
     */
    void updateDuration(Long teachplanId, String fileId, Integer duration);
}
