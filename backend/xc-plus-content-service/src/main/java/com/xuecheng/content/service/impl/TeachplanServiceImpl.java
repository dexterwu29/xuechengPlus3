package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xuecheng.content.config.CompanyContext;
import com.xuecheng.content.exception.BusinessException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.TeachplanDTO;
import com.xuecheng.content.model.entity.CourseBase;
import com.xuecheng.content.model.entity.Teachplan;
import com.xuecheng.content.model.entity.TeachplanMedia;
import com.xuecheng.content.model.vo.TeachplanMediaVO;
import com.xuecheng.content.model.vo.TeachplanTreeVO;
import com.xuecheng.content.service.TeachplanService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 课程计划服务实现
 */
@Service
@RequiredArgsConstructor
public class TeachplanServiceImpl implements TeachplanService {

    private final TeachplanMapper teachplanMapper;
    private final TeachplanMediaMapper teachplanMediaMapper;
    private final CourseBaseMapper courseBaseMapper;

    @Override
    public List<TeachplanTreeVO> listTree(Long courseId) {
        ensureCompanyOwns(courseId);
        List<Teachplan> all = teachplanMapper.selectList(
                new LambdaQueryWrapper<Teachplan>()
                        .eq(Teachplan::getCourseId, courseId)
                        .orderByAsc(Teachplan::getOrderBy)
        );
        List<TeachplanMedia> mediaList = teachplanMediaMapper.selectList(
                new LambdaQueryWrapper<TeachplanMedia>()
                        .eq(TeachplanMedia::getCourseId, courseId)
        );
        Map<Long, TeachplanMediaVO> mediaMap = mediaList.stream()
                .collect(Collectors.toMap(TeachplanMedia::getTeachplanId, this::toMediaVO));

        return buildTree(0L, all, mediaMap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(TeachplanDTO dto) {
        Long courseId = dto.getCourseId();
        if (courseId == null) {
            throw new BusinessException(400, "courseId不能为空");
        }
        ensureCompanyOwns(courseId);

        Long parentId = dto.getParentId() != null ? dto.getParentId() : 0L;
        int grade = parentId == 0 ? 1 : 2;
        Integer maxOrder = teachplanMapper.selectMaxOrderBy(courseId, parentId);
        int orderBy = (maxOrder != null ? maxOrder : 0) + 1;

        Teachplan entity = new Teachplan();
        BeanUtils.copyProperties(dto, entity);
        entity.setCourseId(courseId);
        entity.setParentId(parentId);
        entity.setGrade(grade);
        entity.setOrderBy(orderBy);
        entity.setStatus(1);
        entity.setIsDeleted(0);
        teachplanMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void update(Long id, TeachplanDTO dto) {
        Teachplan existing = teachplanMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "课程计划不存在");
        }
        ensureCompanyOwns(existing.getCourseId());
        Teachplan entity = new Teachplan();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(id);
        teachplanMapper.updateById(entity);
    }

    @Override
    public void remove(Long id) {
        Teachplan existing = teachplanMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "课程计划不存在");
        }
        ensureCompanyOwns(existing.getCourseId());
        teachplanMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveUp(Long id) {
        swapOrder(id, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveDown(Long id) {
        swapOrder(id, false);
    }

    private void swapOrder(Long id, boolean up) {
        Teachplan current = teachplanMapper.selectById(id);
        if (current == null) {
            throw new BusinessException(404, "课程计划不存在");
        }
        ensureCompanyOwns(current.getCourseId());

        LambdaQueryWrapper<Teachplan> wrapper = new LambdaQueryWrapper<Teachplan>()
                .eq(Teachplan::getCourseId, current.getCourseId())
                .eq(Teachplan::getParentId, current.getParentId());
        if (up) {
            wrapper.lt(Teachplan::getOrderBy, current.getOrderBy()).orderByDesc(Teachplan::getOrderBy).last("LIMIT 1");
        } else {
            wrapper.gt(Teachplan::getOrderBy, current.getOrderBy()).orderByAsc(Teachplan::getOrderBy).last("LIMIT 1");
        }
        Teachplan sibling = teachplanMapper.selectOne(wrapper);
        if (sibling == null) return;

        int curOrder = current.getOrderBy();
        int sibOrder = sibling.getOrderBy();
        teachplanMapper.update(null, new LambdaUpdateWrapper<Teachplan>().eq(Teachplan::getId, id).set(Teachplan::getOrderBy, sibOrder));
        teachplanMapper.update(null, new LambdaUpdateWrapper<Teachplan>().eq(Teachplan::getId, sibling.getId()).set(Teachplan::getOrderBy, curOrder));
    }

    private List<TeachplanTreeVO> buildTree(Long parentId, List<Teachplan> all, Map<Long, TeachplanMediaVO> mediaMap) {
        return all.stream()
                .filter(t -> parentId.equals(t.getParentId()))
                .map(t -> toTreeVO(t, mediaMap.get(t.getId())))
                .peek(vo -> vo.setChildren(buildTree(vo.getId(), all, mediaMap)))
                .collect(Collectors.toList());
    }

    private TeachplanTreeVO toTreeVO(Teachplan entity, TeachplanMediaVO media) {
        TeachplanTreeVO vo = new TeachplanTreeVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setTeachplanMedia(media);
        return vo;
    }

    private TeachplanMediaVO toMediaVO(TeachplanMedia entity) {
        TeachplanMediaVO vo = new TeachplanMediaVO();
        BeanUtils.copyProperties(entity, vo);
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
