package com.xuecheng.content.service;

import com.xuecheng.content.model.vo.LoginVO;

/**
 * 认证服务
 */
public interface AuthService {

    /**
     * 登录，返回 JWT
     */
    LoginVO login(String username, String password);
}
