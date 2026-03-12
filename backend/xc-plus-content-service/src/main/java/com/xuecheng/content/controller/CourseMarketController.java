package com.xuecheng.content.controller;

import com.xuecheng.content.common.RestResponse;
import com.xuecheng.content.model.dto.CourseMarketDTO;
import com.xuecheng.content.service.CourseMarketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 课程营销控制器
 */
@Tag(name = "课程营销")
@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseMarketController {

    private final CourseMarketService courseMarketService;

    @Operation(summary = "获取课程营销信息")
    @GetMapping("/{courseId}/market")
    public RestResponse<CourseMarketDTO> getMarket(@PathVariable Long courseId) {
        CourseMarketDTO dto = courseMarketService.getByCourseId(courseId);
        return RestResponse.success(dto);
    }

    @Operation(summary = "保存课程营销信息")
    @PutMapping("/{courseId}/market")
    public RestResponse<Void> saveMarket(@PathVariable Long courseId, @RequestBody CourseMarketDTO dto) {
        courseMarketService.save(courseId, dto);
        return RestResponse.success();
    }
}
