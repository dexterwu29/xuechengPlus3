package com.xuecheng.content.controller;

import com.xuecheng.content.common.RestResponse;
import com.xuecheng.content.model.dto.TeachplanMediaDTO;
import com.xuecheng.content.service.TeachplanMediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 课程计划媒资控制器
 */
@Tag(name = "课程计划媒资")
@RestController
@RequestMapping("/teachplans")
@RequiredArgsConstructor
public class TeachplanMediaController {

    private final TeachplanMediaService teachplanMediaService;

    @Operation(summary = "绑定媒资")
    @PostMapping("/{teachplanId}/media")
    public RestResponse<Void> bindMedia(@PathVariable Long teachplanId, @RequestBody TeachplanMediaDTO dto) {
        teachplanMediaService.bind(teachplanId, dto);
        return RestResponse.success();
    }

    @Operation(summary = "解绑媒资")
    @DeleteMapping("/{teachplanId}/media")
    public RestResponse<Void> unbindMedia(@PathVariable Long teachplanId) {
        teachplanMediaService.unbind(teachplanId);
        return RestResponse.success();
    }
}
