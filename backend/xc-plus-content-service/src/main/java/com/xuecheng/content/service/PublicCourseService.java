package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.PageParams;
import com.xuecheng.content.model.vo.CourseBaseVO;
import com.xuecheng.content.model.vo.CourseDetailVO;
import com.xuecheng.content.model.vo.PageResult;
import com.xuecheng.content.model.vo.TeachplanTreeVO;

import java.util.List;

/**
 * 公开课程服务 - 无需机构标识，仅返回已发布课程
 */
public interface PublicCourseService {

    PageResult<CourseBaseVO> pagePublished(PageParams params, String courseName, String mt, String st);

    CourseDetailVO getPublishedDetail(Long id);

    List<TeachplanTreeVO> getPublishedTeachplans(Long courseId);
}
