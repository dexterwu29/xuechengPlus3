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

import java.time.LocalDateTime;

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
        validateMarket(dto);
        ensureCompanyOwns(courseId);
        CourseMarket entity = courseMarketMapper.selectById(courseId);
        Long companyId = CompanyContext.getCompanyId();
        if (entity == null) {
            entity = new CourseMarket();
            entity.setId(courseId);
            entity.setIsDeleted(0);
            BeanUtils.copyProperties(dto, entity);
            LocalDateTime now = LocalDateTime.now();
            entity.setCreateTime(now);
            entity.setUpdateTime(now);
            entity.setCreateBy(String.valueOf(companyId));
            entity.setUpdateBy(String.valueOf(companyId));
            courseMarketMapper.insert(entity);
        } else {
            BeanUtils.copyProperties(dto, entity);
            entity.setUpdateTime(LocalDateTime.now());
            entity.setUpdateBy(String.valueOf(companyId));
            courseMarketMapper.updateById(entity);
        }
    }

    private void validateMarket(CourseMarketDTO dto) {
        if (dto == null) return;
        if (dto.getCharge() != null && !dto.getCharge().isEmpty()
                && !"201000".equals(dto.getCharge()) && !"201001".equals(dto.getCharge())) {
            throw new BusinessException(400, "收费规则必须为201000免费或201001收费");
        }
        if ("201001".equals(dto.getCharge())) {
            if (dto.getPrice() == null || dto.getPrice() <= 0) {
                throw new BusinessException(400, "收费课程必须填写有效价格且大于0");
            }
            if (dto.getValidDays() == null || dto.getValidDays() <= 0) {
                throw new BusinessException(400, "收费课程必须填写有效期天数且大于0");
            }
        }
        // 文档 3.1：QQ、微信、手机号必填，手机号 1[3-9]\d{9}
        if (dto.getQq() == null || dto.getQq().isBlank()) {
            throw new BusinessException(400, "咨询QQ不能为空");
        }
        if (dto.getWechat() == null || dto.getWechat().isBlank()) {
            throw new BusinessException(400, "微信不能为空");
        }
        if (dto.getPhone() == null || dto.getPhone().isBlank()) {
            throw new BusinessException(400, "手机号不能为空");
        }
        if (!dto.getPhone().trim().matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException(400, "手机号需为11位，1开头，第二位3-9");
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
