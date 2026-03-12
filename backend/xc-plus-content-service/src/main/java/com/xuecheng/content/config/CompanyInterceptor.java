package com.xuecheng.content.config;

import com.xuecheng.content.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 机构隔离拦截器 - 解析 X-Company-Id 请求头
 */
@Component
public class CompanyInterceptor implements HandlerInterceptor {

    public static final String HEADER_COMPANY_ID = "X-Company-Id";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String companyIdStr = request.getHeader(HEADER_COMPANY_ID);
        if (companyIdStr == null || companyIdStr.isBlank()) {
            throw new BusinessException(403, "缺少机构标识，请提供 X-Company-Id 请求头");
        }
        try {
            Long companyId = Long.parseLong(companyIdStr.trim());
            CompanyContext.setCompanyId(companyId);
            return true;
        } catch (NumberFormatException e) {
            throw new BusinessException(400, "X-Company-Id 必须为有效数字");
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        CompanyContext.clear();
    }
}
