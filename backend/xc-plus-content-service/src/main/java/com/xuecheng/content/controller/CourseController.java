package com.xuecheng.content.controller;

import com.xuecheng.content.common.RestResponse;
import com.xuecheng.content.model.dto.CourseCreateDTO;
import com.xuecheng.content.model.dto.CoursePageQueryDTO;
import com.xuecheng.content.model.dto.TeachplanDTO;
import com.xuecheng.content.model.dto.CourseQueryDTO;
import com.xuecheng.content.model.dto.CourseUpdateDTO;
import com.xuecheng.content.model.dto.PageParams;
import com.xuecheng.content.model.vo.CourseBaseVO;
import com.xuecheng.content.model.vo.CourseDetailVO;
import com.xuecheng.content.model.vo.PageResult;
import com.xuecheng.content.model.vo.TeachplanTreeVO;
import com.xuecheng.content.service.CourseBaseService;
import com.xuecheng.content.service.TeachplanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程控制器
 */
@Tag(name = "课程管理")
@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
@Validated
public class CourseController {

    private final CourseBaseService courseBaseService;
    private final TeachplanService teachplanService;

    @Operation(summary = "分页查询课程")
    @PostMapping("/page")
    public RestResponse<PageResult<CourseBaseVO>> pageCourses(@RequestBody @Valid CoursePageQueryDTO body) {
        PageParams params = new PageParams(body.getPageNo(), body.getPageSize());
        CourseQueryDTO queryDTO = new CourseQueryDTO();
        queryDTO.setCourseName(body.getCourseName());
        queryDTO.setAuditStatus(body.getAuditStatus());
        queryDTO.setPublishStatus(body.getPublishStatus());
        queryDTO.setMt(body.getMt());
        queryDTO.setSt(body.getSt());
        PageResult<CourseBaseVO> result = courseBaseService.pageQuery(params, queryDTO);
        return RestResponse.success(result);
    }

    @Operation(summary = "分页查询课程（GET，Query 参数）")
    @GetMapping
    public RestResponse<PageResult<CourseBaseVO>> pageCoursesGet(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String courseName,
            @RequestParam(required = false) String auditStatus,
            @RequestParam(required = false) String publishStatus,
            @RequestParam(required = false) String mt,
            @RequestParam(required = false) String st) {
        PageParams params = new PageParams(pageNo, pageSize);
        CourseQueryDTO queryDTO = new CourseQueryDTO();
        queryDTO.setCourseName(courseName);
        queryDTO.setAuditStatus(auditStatus);
        queryDTO.setPublishStatus(publishStatus);
        queryDTO.setMt(mt);
        queryDTO.setSt(st);
        PageResult<CourseBaseVO> result = courseBaseService.pageQuery(params, queryDTO);
        return RestResponse.success(result);
    }

    @Operation(summary = "新增课程")
    @PostMapping
    public RestResponse<Long> createCourse(@RequestBody @Valid CourseCreateDTO dto) {
        Long id = courseBaseService.create(dto);
        return RestResponse.success(id);
    }

    @Operation(summary = "获取课程详情")
    @GetMapping("/{id}")
    public RestResponse<CourseDetailVO> getCourse(@PathVariable Long id) {
        CourseDetailVO vo = courseBaseService.getById(id);
        return RestResponse.success(vo);
    }

    @Operation(summary = "更新课程")
    @PutMapping("/{id}")
    public RestResponse<Void> updateCourse(@PathVariable Long id, @RequestBody @Valid CourseUpdateDTO dto) {
        courseBaseService.updateById(id, dto);
        return RestResponse.success();
    }

    @Operation(summary = "删除课程")
    @DeleteMapping("/{id}")
    public RestResponse<Void> deleteCourse(@PathVariable Long id) {
        courseBaseService.removeById(id);
        return RestResponse.success();
    }

    @Operation(summary = "获取课程教学计划树")
    @GetMapping("/{id}/teachplans")
    public RestResponse<List<TeachplanTreeVO>> listTeachplans(@PathVariable Long id) {
        List<TeachplanTreeVO> tree = teachplanService.listTree(id);
        return RestResponse.success(tree);
    }

    @Operation(summary = "新增教学计划")
    @PostMapping("/{id}/teachplans")
    public RestResponse<Long> createTeachplan(@PathVariable Long id, @RequestBody @Valid TeachplanDTO dto) {
        dto.setCourseId(id);
        Long planId = teachplanService.create(dto);
        return RestResponse.success(planId);
    }

    @Operation(summary = "课程发布（预留，本阶段不测试）")
    @PostMapping("/{id}/publish")
    public RestResponse<Void> publishCourse(@PathVariable Long id) {
        // 预留：后续实现课程发布逻辑
        return RestResponse.success();
    }
}
