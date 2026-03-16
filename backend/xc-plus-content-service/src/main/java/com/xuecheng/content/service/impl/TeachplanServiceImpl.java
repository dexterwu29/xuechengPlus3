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

import java.time.LocalDateTime;
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
        return listTreeInternal(courseId);
    }

    @Override
    public List<TeachplanTreeVO> listTreeForAdmin(Long courseId) {
        return listTreeInternal(courseId);
    }

    private List<TeachplanTreeVO> listTreeInternal(Long courseId) {
        List<Teachplan> all = teachplanMapper.selectList(
                new LambdaQueryWrapper<Teachplan>()
                        .eq(Teachplan::getCourseId, courseId)
                        .orderByAsc(Teachplan::getOrderBy)
        );
        List<TeachplanMedia> mediaList = teachplanMediaMapper.selectList(
                new LambdaQueryWrapper<TeachplanMedia>()
                        .eq(TeachplanMedia::getCourseId, courseId)
                        .orderByAsc(TeachplanMedia::getOrderBy)
        );
        Map<Long, List<TeachplanMediaVO>> mediaMap = mediaList.stream()
                .map(this::toMediaVO)
                .collect(Collectors.groupingBy(TeachplanMediaVO::getTeachplanId));

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
        Long companyId = CompanyContext.getCompanyId();

        Long parentId = dto.getParentId() != null ? dto.getParentId() : 0L;
        int grade = parentId == 0 ? 1 : 2;
        int orderBy;
        if (dto.getOrderBy() != null && dto.getOrderBy() >= 0) {
            orderBy = dto.getOrderBy();
            // 插入到指定位置：将 orderBy >= orderBy 的兄弟节点后移
            teachplanMapper.shiftOrderByFrom(courseId, parentId, orderBy, 1);
        } else {
            Integer maxOrder = teachplanMapper.selectMaxOrderBy(courseId, parentId);
            orderBy = (maxOrder != null ? maxOrder : 0) + 1;
        }

        Teachplan entity = new Teachplan();
        BeanUtils.copyProperties(dto, entity);
        entity.setCourseId(courseId);
        entity.setParentId(parentId);
        entity.setGrade(grade);
        entity.setOrderBy(orderBy);
        entity.setStatus(1);
        entity.setIsDeleted(0);
        LocalDateTime now = LocalDateTime.now();
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        entity.setCreateBy(String.valueOf(companyId));
        entity.setUpdateBy(String.valueOf(companyId));
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
        entity.setUpdateTime(LocalDateTime.now());
        entity.setUpdateBy(String.valueOf(CompanyContext.getCompanyId()));
        teachplanMapper.updateById(entity);
    }

    @Override
    public void remove(Long id) {
        Teachplan existing = teachplanMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "课程计划不存在");
        }
        ensureCompanyOwns(existing.getCourseId());
        if (existing.getGrade() != null && existing.getGrade() == 1) {
            long childCount = teachplanMapper.selectCount(
                    new LambdaQueryWrapper<Teachplan>().eq(Teachplan::getParentId, id)
            );
            if (childCount > 0) {
                throw new BusinessException(400, "该章下有子节，请先删除所有子节后再删除章");
            }
        }
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
        LocalDateTime now = LocalDateTime.now();
        String by = String.valueOf(CompanyContext.getCompanyId());
        teachplanMapper.update(null, new LambdaUpdateWrapper<Teachplan>().eq(Teachplan::getId, id).set(Teachplan::getOrderBy, sibOrder).set(Teachplan::getUpdateTime, now).set(Teachplan::getUpdateBy, by));
        teachplanMapper.update(null, new LambdaUpdateWrapper<Teachplan>().eq(Teachplan::getId, sibling.getId()).set(Teachplan::getOrderBy, curOrder).set(Teachplan::getUpdateTime, now).set(Teachplan::getUpdateBy, by));
    }

    private List<TeachplanTreeVO> buildTree(Long parentId, List<Teachplan> all, Map<Long, List<TeachplanMediaVO>> mediaMap) {
        return all.stream()
                .filter(t -> parentId.equals(t.getParentId()))
                .map(t -> toTreeVO(t, mediaMap.getOrDefault(t.getId(), List.of())))
                .peek(vo -> vo.setChildren(buildTree(vo.getId(), all, mediaMap)))
                .collect(Collectors.toList());
    }

    private TeachplanTreeVO toTreeVO(Teachplan entity, List<TeachplanMediaVO> mediaList) {
        TeachplanTreeVO vo = new TeachplanTreeVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setMediaList(mediaList);
        vo.setTeachplanMedia(mediaList.isEmpty() ? null : mediaList.get(0));
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
