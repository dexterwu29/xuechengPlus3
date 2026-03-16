package com.xuecheng.content.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录用户信息（从 JWT 解析）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser {
    private Long userId;
    private String username;
    private String role;
    private Long companyId;

    public boolean isSuperAdmin() {
        return "super_admin".equals(role);
    }

    public boolean isTeacher() {
        return "teacher".equals(role);
    }

    public boolean isVisitor() {
        return "visitor".equals(role);
    }

    /** 超级管理员：提交课程时直接发布（新版本仅 super_admin，已移除 admin 角色） */
    public boolean isAdminOrSuperAdmin() {
        return "super_admin".equals(role);
    }
}
