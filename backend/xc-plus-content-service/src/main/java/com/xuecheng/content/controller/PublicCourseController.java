package com.xuecheng.content.controller;

import com.xuecheng.content.common.RestResponse;
import com.xuecheng.content.model.dto.PageParams;
import com.xuecheng.content.model.vo.CourseBaseVO;
import com.xuecheng.content.model.vo.CourseDetailVO;
import com.xuecheng.content.model.vo.PageResult;
import com.xuecheng.content.model.vo.TeachplanTreeVO;
import com.xuecheng.content.service.PublicCourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公开课程接口 - 无需 X-Company-Id
 */
@Tag(name = "公开课程")
@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
public class PublicCourseController {

    private final PublicCourseService publicCourseService;

    @Operation(summary = "已发布课程分页")
    @GetMapping("/courses")
    public RestResponse<PageResult<CourseBaseVO>> pageCourses(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String courseName,
            @RequestParam(required = false) String mt,
            @RequestParam(required = false) String st) {
        PageParams params = new PageParams(pageNo, pageSize);
        return RestResponse.success(publicCourseService.pagePublished(params, courseName, mt, st));
    }

    @Operation(summary = "已发布课程详情")
    @GetMapping("/courses/{id}")
    public RestResponse<CourseDetailVO> getCourse(@PathVariable Long id) {
        return RestResponse.success(publicCourseService.getPublishedDetail(id));
    }

    @Operation(summary = "已发布课程教学计划")
    @GetMapping("/courses/{id}/teachplans")
    public RestResponse<List<TeachplanTreeVO>> getTeachplans(@PathVariable Long id) {
        return RestResponse.success(publicCourseService.getPublishedTeachplans(id));
    }
}
