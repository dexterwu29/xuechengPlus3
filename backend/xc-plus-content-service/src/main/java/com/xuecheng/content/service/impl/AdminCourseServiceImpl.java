package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xuecheng.content.exception.BusinessException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.entity.CourseBase;
import com.xuecheng.content.model.entity.CourseMarket;
import com.xuecheng.content.model.vo.CourseBaseVO;
import com.xuecheng.content.model.vo.CourseDetailVO;
import com.xuecheng.content.service.AdminCourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminCourseServiceImpl implements AdminCourseService {

    private final CourseBaseMapper courseBaseMapper;
    private final CourseMarketMapper courseMarketMapper;

    @Override
    public List<CourseBaseVO> listPending() {
        List<CourseBase> list = courseBaseMapper.selectList(
                new LambdaQueryWrapper<CourseBase>()
                        .eq(CourseBase::getAuditStatus, "202003")
                        .orderByDesc(CourseBase::getUpdateTime)
        );
        return list.stream().map(this::toVO).toList();
    }

    @Override
    public CourseDetailVO getByIdForAudit(Long id) {
        CourseBase base = courseBaseMapper.selectById(id);
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
    public void audit(Long id, boolean approved) {
        CourseBase base = courseBaseMapper.selectById(id);
        if (base == null) {
            throw new BusinessException(404, "课程不存在");
        }
        if (!"202003".equals(base.getAuditStatus())) {
            throw new BusinessException(400, "仅待审核状态可审核");
        }
        String newStatus = approved ? "202004" : "202001";
        courseBaseMapper.update(null, new LambdaUpdateWrapper<CourseBase>()
                .eq(CourseBase::getId, id)
                .set(CourseBase::getAuditStatus, newStatus)
                .set(CourseBase::getUpdateTime, LocalDateTime.now()));
    }

    private CourseBaseVO toVO(CourseBase entity) {
        CourseBaseVO vo = new CourseBaseVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
