package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.content.config.CompanyContext;
import com.xuecheng.content.exception.BusinessException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.CourseCreateDTO;
import com.xuecheng.content.model.dto.CourseQueryDTO;
import com.xuecheng.content.model.dto.CourseUpdateDTO;
import com.xuecheng.content.model.dto.PageParams;
import com.xuecheng.content.model.entity.CourseBase;
import com.xuecheng.content.model.entity.CourseMarket;
import com.xuecheng.content.model.entity.CourseTeacher;
import com.xuecheng.content.model.entity.Teachplan;
import com.xuecheng.content.model.vo.CourseBaseVO;
import com.xuecheng.content.model.vo.CourseDetailVO;
import com.xuecheng.content.model.vo.PageResult;
import com.xuecheng.content.service.CourseBaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 课程基础服务实现
 */
@Service
@RequiredArgsConstructor
public class CourseBaseServiceImpl implements CourseBaseService {

    private final CourseBaseMapper courseBaseMapper;
    private final CourseMarketMapper courseMarketMapper;
    private final CourseTeacherMapper courseTeacherMapper;
    private final TeachplanMapper teachplanMapper;

    @Override
    public PageResult<CourseBaseVO> pageQuery(PageParams pageParams, CourseQueryDTO queryDTO) {
        Long companyId = CompanyContext.getCompanyId();
        LambdaQueryWrapper<CourseBase> wrapper = new LambdaQueryWrapper<CourseBase>()
                .eq(CourseBase::getCompanyId, companyId);

        if (queryDTO != null) {
            if (StringUtils.hasText(queryDTO.getCourseName())) {
                wrapper.like(CourseBase::getName, queryDTO.getCourseName());
            }
            if (StringUtils.hasText(queryDTO.getAuditStatus())) {
                wrapper.eq(CourseBase::getAuditStatus, queryDTO.getAuditStatus());
            }
            if (StringUtils.hasText(queryDTO.getPublishStatus())) {
                wrapper.eq(CourseBase::getPublishStatus, queryDTO.getPublishStatus());
            }
        }

        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        Page<CourseBase> result = courseBaseMapper.selectPage(page, wrapper);

        List<CourseBaseVO> items = result.getRecords().stream()
                .map(this::toVO)
                .toList();

        return new PageResult<>(items, result.getTotal(), pageParams.getPageNo(), pageParams.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(CourseCreateDTO dto) {
        Long companyId = CompanyContext.getCompanyId();
        CourseBase entity = new CourseBase();
        BeanUtils.copyProperties(dto, entity);
        entity.setCompanyId(companyId);
        entity.setAuditStatus("202002");
        entity.setPublishStatus("203001");
        entity.setIsDeleted(0);
        LocalDateTime now = LocalDateTime.now();
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        entity.setCreateBy(String.valueOf(companyId));
        entity.setUpdateBy(String.valueOf(companyId));
        courseBaseMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public CourseDetailVO getById(Long id) {
        Long companyId = CompanyContext.getCompanyId();
        CourseBase base = courseBaseMapper.selectOne(
                new LambdaQueryWrapper<CourseBase>()
                        .eq(CourseBase::getId, id)
                        .eq(CourseBase::getCompanyId, companyId)
        );
        if (base == null) {
            throw new BusinessException(404, "课程不存在");
        }
        CourseDetailVO vo = new CourseDetailVO();
        BeanUtils.copyProperties(base, vo);

        CourseMarket market = courseMarketMapper.selectById(id);
        if (market != null) {
            vo.setCharge(market.getCharge());
            vo.setPrice(market.getPrice());
            vo.setOriginalPrice(market.getOriginalPrice());
            vo.setQq(market.getQq());
            vo.setWechat(market.getWechat());
            vo.setPhone(market.getPhone());
            vo.setValidDays(market.getValidDays());
        }
        return vo;
    }

    @Override
    public void updateById(Long id, CourseUpdateDTO dto) {
        ensureCompanyOwns(id);
        CourseBase entity = new CourseBase();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(id);
        entity.setUpdateTime(LocalDateTime.now());
        entity.setUpdateBy(String.valueOf(CompanyContext.getCompanyId()));
        courseBaseMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeById(Long id) {
        ensureCompanyOwns(id);
        courseBaseMapper.deleteById(id);
        courseMarketMapper.delete(new LambdaQueryWrapper<CourseMarket>().eq(CourseMarket::getId, id));
        LocalDateTime now = LocalDateTime.now();
        String by = String.valueOf(CompanyContext.getCompanyId());
        courseTeacherMapper.update(null, new LambdaUpdateWrapper<CourseTeacher>()
                .set(CourseTeacher::getIsDeleted, 1)
                .set(CourseTeacher::getUpdateTime, now)
                .set(CourseTeacher::getUpdateBy, by)
                .eq(CourseTeacher::getCourseId, id));
        teachplanMapper.update(null, new LambdaUpdateWrapper<Teachplan>()
                .set(Teachplan::getIsDeleted, 1)
                .set(Teachplan::getUpdateTime, now)
                .set(Teachplan::getUpdateBy, by)
                .eq(Teachplan::getCourseId, id));
    }

    private void ensureCompanyOwns(Long courseId) {
        Long companyId = CompanyContext.getCompanyId();
        CourseBase base = courseBaseMapper.selectById(courseId);
        if (base == null || !companyId.equals(base.getCompanyId())) {
            throw new BusinessException(403, "无权操作该课程");
        }
    }

    private CourseBaseVO toVO(CourseBase entity) {
        CourseBaseVO vo = new CourseBaseVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
