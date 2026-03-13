package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CourseCreateDTO;
import com.xuecheng.content.model.dto.CourseQueryDTO;
import com.xuecheng.content.model.dto.CourseUpdateDTO;
import com.xuecheng.content.model.dto.PageParams;
import com.xuecheng.content.model.vo.CourseBaseVO;
import com.xuecheng.content.model.vo.CourseDetailVO;
import com.xuecheng.content.model.vo.PageResult;

/**
 * 课程基础服务
 */
public interface CourseBaseService {

    PageResult<CourseBaseVO> pageQuery(PageParams pageParams, CourseQueryDTO queryDTO);

    Long create(CourseCreateDTO dto);

    CourseDetailVO getById(Long id);

    void updateById(Long id, CourseUpdateDTO dto);

    void removeById(Long id);

    void updatePic(Long id, String fileId);

    /**
     * 教师提交审核（草稿 202002 -> 待审核 202003）
     */
    void submit(Long id);

    void publish(Long id);
}
