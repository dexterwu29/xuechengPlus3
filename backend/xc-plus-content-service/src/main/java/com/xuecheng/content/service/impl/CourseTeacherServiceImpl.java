package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.content.config.CompanyContext;
import com.xuecheng.content.exception.BusinessException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.dto.CourseTeacherDTO;
import com.xuecheng.content.model.entity.CourseBase;
import com.xuecheng.content.model.entity.CourseTeacher;
import com.xuecheng.content.model.vo.CourseTeacherVO;
import com.xuecheng.content.service.CourseTeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 课程教师服务实现
 */
@Service
@RequiredArgsConstructor
public class CourseTeacherServiceImpl implements CourseTeacherService {

    private final CourseTeacherMapper courseTeacherMapper;
    private final CourseBaseMapper courseBaseMapper;

    @Override
    public List<CourseTeacherVO> listByCourseId(Long courseId) {
        ensureCompanyOwns(courseId);
        List<CourseTeacher> list = courseTeacherMapper.selectList(
                new LambdaQueryWrapper<CourseTeacher>()
                        .eq(CourseTeacher::getCourseId, courseId)
        );
        return list.stream().map(this::toVO).toList();
    }

    @Override
    public CourseTeacherVO getById(Long courseId, Long teacherId) {
        ensureCompanyOwns(courseId);
        CourseTeacher entity = courseTeacherMapper.selectOne(
                new LambdaQueryWrapper<CourseTeacher>()
                        .eq(CourseTeacher::getId, teacherId)
                        .eq(CourseTeacher::getCourseId, courseId)
        );
        if (entity == null) {
            throw new BusinessException(404, "教师不存在");
        }
        return toVO(entity);
    }

    @Override
    public Long create(Long courseId, CourseTeacherDTO dto) {
        ensureCompanyOwns(courseId);
        CourseTeacher entity = new CourseTeacher();
        BeanUtils.copyProperties(dto, entity);
        entity.setCourseId(courseId);
        entity.setIsDeleted(0);
        courseTeacherMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void update(Long courseId, Long teacherId, CourseTeacherDTO dto) {
        ensureCompanyOwns(courseId);
        CourseTeacher existing = courseTeacherMapper.selectOne(
                new LambdaQueryWrapper<CourseTeacher>()
                        .eq(CourseTeacher::getId, teacherId)
                        .eq(CourseTeacher::getCourseId, courseId)
        );
        if (existing == null) {
            throw new BusinessException(404, "教师不存在");
        }
        CourseTeacher entity = new CourseTeacher();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(teacherId);
        courseTeacherMapper.updateById(entity);
    }

    @Override
    public void remove(Long courseId, Long teacherId) {
        ensureCompanyOwns(courseId);
        CourseTeacher existing = courseTeacherMapper.selectOne(
                new LambdaQueryWrapper<CourseTeacher>()
                        .eq(CourseTeacher::getId, teacherId)
                        .eq(CourseTeacher::getCourseId, courseId)
        );
        if (existing == null) {
            throw new BusinessException(404, "教师不存在");
        }
        courseTeacherMapper.deleteById(teacherId);
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

    private CourseTeacherVO toVO(CourseTeacher entity) {
        CourseTeacherVO vo = new CourseTeacherVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
