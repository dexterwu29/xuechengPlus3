package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.content.config.CompanyContext;
import com.xuecheng.content.exception.BusinessException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.TeachplanMediaDTO;
import com.xuecheng.content.model.entity.CourseBase;
import com.xuecheng.content.model.entity.Teachplan;
import com.xuecheng.content.model.entity.TeachplanMedia;
import com.xuecheng.content.service.TeachplanMediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 课程计划媒资服务实现
 */
@Service
@RequiredArgsConstructor
public class TeachplanMediaServiceImpl implements TeachplanMediaService {

    private final TeachplanMediaMapper teachplanMediaMapper;
    private final TeachplanMapper teachplanMapper;
    private final CourseBaseMapper courseBaseMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bind(Long teachplanId, TeachplanMediaDTO dto) {
        Teachplan plan = teachplanMapper.selectById(teachplanId);
        if (plan == null) {
            throw new BusinessException(404, "课程计划不存在");
        }
        ensureCompanyOwns(plan.getCourseId());

        TeachplanMedia existing = teachplanMediaMapper.selectOne(
                new LambdaQueryWrapper<TeachplanMedia>().eq(TeachplanMedia::getTeachplanId, teachplanId)
        );
        if (existing != null) {
            BeanUtils.copyProperties(dto, existing);
            teachplanMediaMapper.updateById(existing);
        } else {
            TeachplanMedia entity = new TeachplanMedia();
            BeanUtils.copyProperties(dto, entity);
            entity.setTeachplanId(teachplanId);
            entity.setCourseId(plan.getCourseId());
            entity.setIsDeleted(0);
            teachplanMediaMapper.insert(entity);
        }
    }

    @Override
    public void unbind(Long teachplanId) {
        Teachplan plan = teachplanMapper.selectById(teachplanId);
        if (plan == null) {
            throw new BusinessException(404, "课程计划不存在");
        }
        ensureCompanyOwns(plan.getCourseId());
        teachplanMediaMapper.delete(new LambdaQueryWrapper<TeachplanMedia>().eq(TeachplanMedia::getTeachplanId, teachplanId));
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
