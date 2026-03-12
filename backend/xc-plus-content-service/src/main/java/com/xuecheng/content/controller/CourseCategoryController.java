package com.xuecheng.content.controller;

import com.xuecheng.content.common.RestResponse;
import com.xuecheng.content.model.vo.CourseCategoryTreeVO;
import com.xuecheng.content.service.CourseCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 课程分类控制器
 */
@Tag(name = "课程分类")
@RestController
@RequestMapping("/course-categories")
@RequiredArgsConstructor
public class CourseCategoryController {

    private final CourseCategoryService courseCategoryService;

    @Operation(summary = "获取分类树")
    @GetMapping("/tree")
    public RestResponse<List<CourseCategoryTreeVO>> listTree() {
        List<CourseCategoryTreeVO> tree = courseCategoryService.listTree();
        return RestResponse.success(tree);
    }
}
