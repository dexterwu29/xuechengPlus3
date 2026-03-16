package com.xuecheng.content.controller;

import com.xuecheng.content.common.RestResponse;
import com.xuecheng.content.model.dto.ArchiveDTO;
import com.xuecheng.content.model.dto.PageParams;
import com.xuecheng.content.model.vo.ArchiveVO;
import com.xuecheng.content.model.vo.PageResult;
import com.xuecheng.content.service.ArchiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 平台备案控制器（机构/讲师/销售）
 */
@Tag(name = "备案管理")
@RestController
@RequestMapping("/archives")
@RequiredArgsConstructor
@Validated
public class ArchiveController {

    private final ArchiveService archiveService;

    @Operation(summary = "分页查询备案")
    @GetMapping
    public RestResponse<PageResult<ArchiveVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String archiveType,
            @RequestParam(required = false) String status) {
        PageParams params = new PageParams(pageNo, pageSize);
        PageResult<ArchiveVO> result = archiveService.pageQuery(params, archiveType, status);
        return RestResponse.success(result);
    }

    @Operation(summary = "获取备案详情")
    @GetMapping("/{id}")
    public RestResponse<ArchiveVO> getById(@PathVariable Long id) {
        return RestResponse.success(archiveService.getById(id));
    }

    @Operation(summary = "新增备案")
    @PostMapping
    public RestResponse<Long> create(@RequestBody @Valid ArchiveDTO dto) {
        return RestResponse.success(archiveService.create(dto));
    }

    @Operation(summary = "更新备案")
    @PutMapping("/{id}")
    public RestResponse<Void> update(@PathVariable Long id, @RequestBody @Valid ArchiveDTO dto) {
        archiveService.updateById(id, dto);
        return RestResponse.success();
    }

    @Operation(summary = "删除备案")
    @DeleteMapping("/{id}")
    public RestResponse<Void> delete(@PathVariable Long id) {
        archiveService.removeById(id);
        return RestResponse.success();
    }

    @Operation(summary = "审核通过")
    @PostMapping("/{id}/approve")
    public RestResponse<Void> approve(@PathVariable Long id) {
        archiveService.approve(id);
        return RestResponse.success();
    }

    @Operation(summary = "审核拒绝")
    @PostMapping("/{id}/reject")
    public RestResponse<Void> reject(@PathVariable Long id) {
        archiveService.reject(id);
        return RestResponse.success();
    }
}
