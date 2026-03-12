package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.TeachplanMediaDTO;

/**
 * 课程计划媒资服务
 */
public interface TeachplanMediaService {

    void bind(Long teachplanId, TeachplanMediaDTO dto);

    void unbind(Long teachplanId);
}
