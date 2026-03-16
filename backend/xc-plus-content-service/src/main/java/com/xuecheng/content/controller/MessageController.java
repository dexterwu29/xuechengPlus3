package com.xuecheng.content.controller;

import com.xuecheng.content.common.RestResponse;
import com.xuecheng.content.model.vo.PageResult;
import com.xuecheng.content.model.vo.SystemMessageVO;
import com.xuecheng.content.security.LoginUser;
import com.xuecheng.content.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 站内信接口
 */
@Tag(name = "站内信")
@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @Operation(summary = "分页查询消息")
    @GetMapping
    public RestResponse<PageResult<SystemMessageVO>> list(
            @AuthenticationPrincipal LoginUser user,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize) {
        if (user == null) {
            return RestResponse.fail(401, "未登录");
        }
        PageResult<SystemMessageVO> result = messageService.list(user.getUserId(), user.getRole(), pageNo, pageSize);
        return RestResponse.success(result);
    }

    @Operation(summary = "未读数量")
    @GetMapping("/unread-count")
    public RestResponse<Long> unreadCount(@AuthenticationPrincipal LoginUser user) {
        if (user == null) {
            return RestResponse.success(0L);
        }
        long count = messageService.unreadCount(user.getUserId(), user.getRole());
        return RestResponse.success(count);
    }

    @Operation(summary = "标记已读")
    @PutMapping("/{id}/read")
    public RestResponse<Void> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal LoginUser user) {
        if (user == null) {
            return RestResponse.fail(401, "未登录");
        }
        messageService.markAsRead(id, user.getUserId(), user.getRole());
        return RestResponse.success();
    }
}
