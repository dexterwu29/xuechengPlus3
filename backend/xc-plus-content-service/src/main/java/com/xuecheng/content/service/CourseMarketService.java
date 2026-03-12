package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CourseMarketDTO;

/**
 * 课程营销服务
 */
public interface CourseMarketService {

    CourseMarketDTO getByCourseId(Long courseId);

    void save(Long courseId, CourseMarketDTO dto);
}
