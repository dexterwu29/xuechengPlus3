package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.content.config.CompanyContext;
import com.xuecheng.content.exception.BusinessException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.dto.CourseMarketDTO;
import com.xuecheng.content.model.entity.CourseBase;
import com.xuecheng.content.model.entity.CourseMarket;
import com.xuecheng.content.service.CourseMarketService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 课程营销服务实现
 */
@Service
@RequiredArgsConstructor
public class CourseMarketServiceImpl implements CourseMarketService {

    private final CourseMarketMapper courseMarketMapper;
    private final CourseBaseMapper courseBaseMapper;

    @Override
    public CourseMarketDTO getByCourseId(Long courseId) {
        ensureCompanyOwns(courseId);
        CourseMarket entity = courseMarketMapper.selectById(courseId);
        if (entity == null) {
            return new CourseMarketDTO();
        }
        CourseMarketDTO dto = new CourseMarketDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(Long courseId, CourseMarketDTO dto) {
        ensureCompanyOwns(courseId);
        CourseMarket entity = courseMarketMapper.selectById(courseId);
        if (entity == null) {
            entity = new CourseMarket();
            entity.setId(courseId);
            entity.setIsDeleted(0);
            BeanUtils.copyProperties(dto, entity);
            courseMarketMapper.insert(entity);
        } else {
            BeanUtils.copyProperties(dto, entity);
            courseMarketMapper.updateById(entity);
        }
    }

    private void ensureCompanyOwns(Long courseId) {
        Long companyId = CompanyContext.getCompanyId();
        CourseBase base = courseBaseMapper.selectOne(
                new LambdaQueryWrapper<CourseBase>()
                        .eq(CourseBase::getId, courseId)
                        .eq(CourseBase::getCompanyId, companyId)
        );
        if (base == null) {
            throw new BusinessException(403, "无权操作该课程");
        }
    }
}
