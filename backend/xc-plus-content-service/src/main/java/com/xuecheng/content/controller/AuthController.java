package com.xuecheng.content.controller;

import com.xuecheng.content.common.RestResponse;
import com.xuecheng.content.model.vo.LoginVO;
import com.xuecheng.content.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 认证接口
 */
@Tag(name = "认证")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "登录")
    @PostMapping("/login")
    public RestResponse<LoginVO> login(
            @RequestParam @NotBlank String username,
            @RequestParam @NotBlank String password
    ) {
        LoginVO vo = authService.login(username, password);
        return RestResponse.success(vo);
    }
}
