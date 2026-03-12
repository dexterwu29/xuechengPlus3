package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.entity.CourseCategory;
import com.xuecheng.content.model.vo.CourseCategoryTreeVO;
import com.xuecheng.content.service.CourseCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 课程分类服务实现
 */
@Service
@RequiredArgsConstructor
public class CourseCategoryServiceImpl implements CourseCategoryService {

    private final CourseCategoryMapper courseCategoryMapper;

    @Override
    public List<CourseCategoryTreeVO> listTree() {
        List<CourseCategory> all = courseCategoryMapper.selectList(
                new LambdaQueryWrapper<CourseCategory>()
                        .eq(CourseCategory::getIsVisible, 1)
                        .orderByAsc(CourseCategory::getOrderBy)
        );
        return buildTree("1", all);
    }

    private List<CourseCategoryTreeVO> buildTree(String parentId, List<CourseCategory> all) {
        return all.stream()
                .filter(c -> parentId.equals(c.getParentId()))
                .map(this::toTreeVO)
                .peek(vo -> vo.setChildrenTreeNodes(buildTree(vo.getId(), all)))
                .collect(Collectors.toList());
    }

    private CourseCategoryTreeVO toTreeVO(CourseCategory entity) {
        CourseCategoryTreeVO vo = new CourseCategoryTreeVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
