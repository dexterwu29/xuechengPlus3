package com.xuecheng.content.service;

import com.xuecheng.content.model.vo.CourseCategoryTreeVO;

import java.util.List;

/**
 * 课程分类服务
 */
public interface CourseCategoryService {

    /**
     * 获取分类树（根节点 id=1）
     */
    List<CourseCategoryTreeVO> listTree();
}
