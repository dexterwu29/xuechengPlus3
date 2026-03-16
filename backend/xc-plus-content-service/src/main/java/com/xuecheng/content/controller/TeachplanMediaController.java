package com.xuecheng.content.controller;

import com.xuecheng.content.common.RestResponse;
import com.xuecheng.content.model.dto.TeachplanMediaDTO;
import com.xuecheng.content.model.vo.TeachplanMediaVO;
import com.xuecheng.content.service.TeachplanMediaService;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
    public RestResponse<Void> bindMedia(@PathVariable Long teachplanId, @RequestBody @Valid TeachplanMediaDTO dto) {
        teachplanMediaService.bind(teachplanId, dto);
        return RestResponse.success();
    }

    @Operation(summary = "解绑该节全部媒资")
    @DeleteMapping("/{teachplanId}/media")
    public RestResponse<Void> unbindMedia(@PathVariable Long teachplanId) {
        teachplanMediaService.unbind(teachplanId);
        return RestResponse.success();
    }

    @Operation(summary = "解绑指定媒资")
    @DeleteMapping("/{teachplanId}/media/{fileId}")
    public RestResponse<Void> unbindMediaByFileId(
            @PathVariable Long teachplanId,
            @PathVariable String fileId) {
        teachplanMediaService.unbindMedia(teachplanId, fileId);
        return RestResponse.success();
    }

    @Operation(summary = "查询该节媒资列表")
    @GetMapping("/{teachplanId}/media")
    public RestResponse<List<TeachplanMediaVO>> listMedia(@PathVariable Long teachplanId) {
        return RestResponse.success(teachplanMediaService.listMedia(teachplanId));
    }

    @Operation(summary = "更新视频时长（31.4）")
    @PutMapping("/{teachplanId}/media/{fileId}/duration")
    public RestResponse<Void> updateMediaDuration(
            @PathVariable Long teachplanId,
            @PathVariable String fileId,
            @RequestBody java.util.Map<String, Integer> body) {
        Integer duration = body.get("duration");
        if (duration == null || duration < 0) {
            return RestResponse.fail(400, "时长参数无效");
        }
        teachplanMediaService.updateDuration(teachplanId, fileId, duration);
        return RestResponse.success();
    }
}
