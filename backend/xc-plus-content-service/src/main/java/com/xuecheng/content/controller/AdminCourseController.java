package com.xuecheng.content.controller;

import com.xuecheng.content.common.RestResponse;
import com.xuecheng.content.model.dto.AuditDTO;
import com.xuecheng.content.model.vo.CourseBaseVO;
import com.xuecheng.content.model.vo.CourseDetailVO;
import com.xuecheng.content.service.AdminCourseService;
import com.xuecheng.content.service.TeachplanService;
import com.xuecheng.content.model.vo.TeachplanTreeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

/**
 * 管理员课程接口（SuperAdmin）
 */
@Tag(name = "管理员-课程审核")
@RestController
@RequestMapping("/admin/courses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminCourseController {

    private final AdminCourseService adminCourseService;
    private final TeachplanService teachplanService;

    @Operation(summary = "待审核课程列表")
    @GetMapping("/pending")
    public RestResponse<List<CourseBaseVO>> listPending() {
        return RestResponse.success(adminCourseService.listPending());
    }

    @Operation(summary = "课程详情（审核预览，需传 X-Company-Id 以加载教学计划）")
    @GetMapping("/{id}")
    public RestResponse<CourseDetailVO> getForAudit(@PathVariable Long id) {
        return RestResponse.success(adminCourseService.getByIdForAudit(id));
    }

    @Operation(summary = "课程教学计划树（审核预览）")
    @GetMapping("/{id}/teachplans")
    public RestResponse<List<TeachplanTreeVO>> listTeachplans(@PathVariable Long id) {
        List<TeachplanTreeVO> tree = teachplanService.listTreeForAdmin(id);
        return RestResponse.success(tree);
    }

    @Operation(summary = "审核：approve通过 / reject退回 / ban封禁")
    @PostMapping("/{id}/audit")
    public RestResponse<Void> audit(
            @PathVariable Long id,
            @RequestBody @Valid AuditDTO dto) {
        adminCourseService.audit(id, dto.getAction(), dto.getOpinion());
        return RestResponse.success();
    }
}
