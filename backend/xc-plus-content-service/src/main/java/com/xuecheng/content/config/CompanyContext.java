package com.xuecheng.content.config;

/**
 * 机构上下文 - 存储当前请求的机构ID
 */
public final class CompanyContext {

    private static final ThreadLocal<Long> COMPANY_ID = new ThreadLocal<>();

    public static void setCompanyId(Long companyId) {
        COMPANY_ID.set(companyId);
    }

    public static Long getCompanyId() {
        return COMPANY_ID.get();
    }

    public static void clear() {
        COMPANY_ID.remove();
    }
}
