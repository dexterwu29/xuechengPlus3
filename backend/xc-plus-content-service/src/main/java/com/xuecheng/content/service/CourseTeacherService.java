package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CourseTeacherDTO;
import com.xuecheng.content.model.vo.CourseTeacherVO;

import java.util.List;

/**
 * 课程教师服务
 */
public interface CourseTeacherService {

    List<CourseTeacherVO> listByCourseId(Long courseId);

    CourseTeacherVO getById(Long courseId, Long teacherId);

    Long create(Long courseId, CourseTeacherDTO dto);

    void update(Long courseId, Long teacherId, CourseTeacherDTO dto);

    void remove(Long courseId, Long teacherId);
}
