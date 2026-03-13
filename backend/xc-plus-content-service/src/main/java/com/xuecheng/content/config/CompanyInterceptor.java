package com.xuecheng.content.config;

import com.xuecheng.content.exception.BusinessException;
import com.xuecheng.content.security.LoginUser;
import com.xuecheng.content.security.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 机构隔离拦截器 - 解析 X-Company-Id 请求头，或从 JWT 登录用户获取（教师）
 */
@Component
public class CompanyInterceptor implements HandlerInterceptor {

    public static final String HEADER_COMPANY_ID = "X-Company-Id";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String companyIdStr = request.getHeader(HEADER_COMPANY_ID);
        Long companyId = null;
        if (companyIdStr != null && !companyIdStr.isBlank()) {
            try {
                companyId = Long.parseLong(companyIdStr.trim());
            } catch (NumberFormatException e) {
                throw new BusinessException(400, "X-Company-Id 必须为有效数字");
            }
        } else {
            LoginUser user = UserContext.get();
            if (user != null && user.isTeacher() && user.getCompanyId() != null) {
                companyId = user.getCompanyId();
            }
        }
        if (companyId == null) {
            throw new BusinessException(403, "缺少机构标识，请提供 X-Company-Id 请求头或使用教师账号登录");
        }
        CompanyContext.setCompanyId(companyId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        CompanyContext.clear();
    }
}
