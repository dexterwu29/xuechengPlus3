package com.xuecheng.content.controller;

import com.xuecheng.content.common.RestResponse;
import com.xuecheng.content.model.dto.TeachplanDTO;
import com.xuecheng.content.service.TeachplanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 课程计划控制器（修改、删除、上移、下移）
 * 获取树、新增计划见 CourseController
 */
@Tag(name = "课程计划")
@RestController
@RequestMapping("/teachplans")
@RequiredArgsConstructor
public class TeachplanController {

    private final TeachplanService teachplanService;

    @Operation(summary = "修改教学计划")
    @PutMapping("/{id}")
    public RestResponse<Void> updateTeachplan(@PathVariable Long id, @RequestBody TeachplanDTO dto) {
        teachplanService.update(id, dto);
        return RestResponse.success();
    }

    @Operation(summary = "逻辑删除教学计划")
    @DeleteMapping("/{id}")
    public RestResponse<Void> deleteTeachplan(@PathVariable Long id) {
        teachplanService.remove(id);
        return RestResponse.success();
    }

    @Operation(summary = "上移")
    @PostMapping("/{id}/move-up")
    public RestResponse<Void> moveUp(@PathVariable Long id) {
        teachplanService.moveUp(id);
        return RestResponse.success();
    }

    @Operation(summary = "下移")
    @PostMapping("/{id}/move-down")
    public RestResponse<Void> moveDown(@PathVariable Long id) {
        teachplanService.moveDown(id);
        return RestResponse.success();
    }
}
