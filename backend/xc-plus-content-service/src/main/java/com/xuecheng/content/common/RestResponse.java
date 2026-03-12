package com.xuecheng.content.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应结构
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestResponse<T> {

    private int code;
    private String msg;
    private T data;

    public static <T> RestResponse<T> success(T data) {
        return new RestResponse<>(0, "success", data);
    }

    public static <T> RestResponse<T> success() {
        return success(null);
    }

    public static <T> RestResponse<T> fail(int code, String msg) {
        return new RestResponse<>(code, msg, null);
    }

    public static <T> RestResponse<T> fail(String msg) {
        return fail(500, msg);
    }
}
