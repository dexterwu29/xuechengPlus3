package com.xuecheng.content.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    /** 数据列表 */
    private List<T> items;
    /** 总记录数 */
    private Long total;
    /** 当前页码 */
    private Integer pageNo;
    /** 每页条数 */
    private Integer pageSize;
}
