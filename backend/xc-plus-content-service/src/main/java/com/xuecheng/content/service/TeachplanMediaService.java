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
}
