package com.xuecheng.content.controller;

import com.xuecheng.content.common.RestResponse;
import com.xuecheng.content.model.dto.CourseTeacherDTO;
import com.xuecheng.content.model.vo.CourseTeacherVO;
import com.xuecheng.content.service.CourseTeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程教师控制器
 */
@Tag(name = "课程教师")
@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseTeacherController {

    private final CourseTeacherService courseTeacherService;

    @Operation(summary = "获取课程教师列表")
    @GetMapping("/{courseId}/teachers")
    public RestResponse<List<CourseTeacherVO>> listTeachers(@PathVariable Long courseId) {
        List<CourseTeacherVO> list = courseTeacherService.listByCourseId(courseId);
        return RestResponse.success(list);
    }

    @Operation(summary = "获取单个教师详情")
    @GetMapping("/{courseId}/teachers/{teacherId}")
    public RestResponse<CourseTeacherVO> getTeacher(@PathVariable Long courseId, @PathVariable Long teacherId) {
        CourseTeacherVO vo = courseTeacherService.getById(courseId, teacherId);
        return RestResponse.success(vo);
    }

    @Operation(summary = "新增教师")
    @PostMapping("/{courseId}/teachers")
    public RestResponse<Long> createTeacher(@PathVariable Long courseId, @RequestBody @Valid CourseTeacherDTO dto) {
        Long id = courseTeacherService.create(courseId, dto);
        return RestResponse.success(id);
    }

    @Operation(summary = "修改教师")
    @PutMapping("/{courseId}/teachers/{teacherId}")
    public RestResponse<Void> updateTeacher(@PathVariable Long courseId, @PathVariable Long teacherId, @RequestBody @Valid CourseTeacherDTO dto) {
        courseTeacherService.update(courseId, teacherId, dto);
        return RestResponse.success();
    }

    @Operation(summary = "逻辑删除教师")
    @DeleteMapping("/{courseId}/teachers/{teacherId}")
    public RestResponse<Void> deleteTeacher(@PathVariable Long courseId, @PathVariable Long teacherId) {
        courseTeacherService.remove(courseId, teacherId);
        return RestResponse.success();
    }
}
