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
import com.xuecheng.content.model.vo.TeachplanMediaVO;
import com.xuecheng.content.service.TeachplanMediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
        String fileId = (dto.getFileId() != null && !dto.getFileId().isBlank())
                ? dto.getFileId().trim()
                : (dto.getMediaId() != null && !dto.getMediaId().isBlank() ? dto.getMediaId().trim() : null);
        if (fileId == null) {
            throw new BusinessException(400, "fileId 或 mediaId 不能为空");
        }

        Teachplan plan = teachplanMapper.selectById(teachplanId);
        if (plan == null) {
            throw new BusinessException(404, "课程计划不存在");
        }
        ensureCompanyOwns(plan.getCourseId());
        Long companyId = CompanyContext.getCompanyId();
        LocalDateTime now = LocalDateTime.now();
        String by = String.valueOf(companyId);

        TeachplanMedia entity = new TeachplanMedia();
        entity.setMediaId(fileId);
        entity.setFileId(fileId);
        entity.setTeachplanId(teachplanId);
        entity.setCourseId(plan.getCourseId());
        entity.setMediaFileName(dto.getMediaFileName() != null ? dto.getMediaFileName() : "");
        entity.setMediaType(dto.getMediaType() != null ? dto.getMediaType() : "video");
        entity.setOrderBy(dto.getOrderBy() != null ? dto.getOrderBy() : 0);
        entity.setIsDeleted(0);
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        entity.setCreateBy(by);
        entity.setUpdateBy(by);
        teachplanMediaMapper.insert(entity);
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

    @Override
    public void unbindMedia(Long teachplanId, String fileId) {
        Teachplan plan = teachplanMapper.selectById(teachplanId);
        if (plan == null) {
            throw new BusinessException(404, "课程计划不存在");
        }
        ensureCompanyOwns(plan.getCourseId());
        teachplanMediaMapper.delete(new LambdaQueryWrapper<TeachplanMedia>()
                .eq(TeachplanMedia::getTeachplanId, teachplanId)
                .eq(TeachplanMedia::getFileId, fileId));
    }

    @Override
    public List<TeachplanMediaVO> listMedia(Long teachplanId) {
        Teachplan plan = teachplanMapper.selectById(teachplanId);
        if (plan == null) {
            throw new BusinessException(404, "课程计划不存在");
        }
        ensureCompanyOwns(plan.getCourseId());
        List<TeachplanMedia> list = teachplanMediaMapper.selectList(
                new LambdaQueryWrapper<TeachplanMedia>()
                        .eq(TeachplanMedia::getTeachplanId, teachplanId)
                        .orderByAsc(TeachplanMedia::getOrderBy));
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    private TeachplanMediaVO toVO(TeachplanMedia e) {
        TeachplanMediaVO vo = new TeachplanMediaVO();
        vo.setId(e.getId());
        vo.setMediaId(e.getMediaId());
        vo.setFileId(e.getFileId());
        vo.setTeachplanId(e.getTeachplanId());
        vo.setCourseId(e.getCourseId());
        vo.setMediaFileName(e.getMediaFileName());
        vo.setMediaType(e.getMediaType());
        vo.setOrderBy(e.getOrderBy());
        return vo;
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
