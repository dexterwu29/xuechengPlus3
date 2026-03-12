package com.xuecheng.content.model.dto;

import lombok.Data;

/**
 * 分页参数
 */
@Data
public class PageParams {

    /** 页码，从1开始 */
    private Integer pageNo = 1;
    /** 每页条数 */
    private Integer pageSize = 10;

    public PageParams() {}

    public PageParams(Integer pageNo, Integer pageSize) {
        this.pageNo = pageNo != null ? pageNo : 1;
        this.pageSize = pageSize != null ? pageSize : 10;
    }
}
