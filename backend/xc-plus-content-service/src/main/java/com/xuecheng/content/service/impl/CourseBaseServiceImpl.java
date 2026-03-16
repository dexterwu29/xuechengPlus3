package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.content.config.CompanyContext;
import com.xuecheng.content.exception.BusinessException;
import com.xuecheng.content.mapper.CourseAuditLogMapper;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.CourseCreateDTO;
import com.xuecheng.content.model.dto.CourseQueryDTO;
import com.xuecheng.content.model.dto.CourseUpdateDTO;
import com.xuecheng.content.model.dto.PageParams;
import com.xuecheng.content.model.enums.AuditAction;
import com.xuecheng.content.model.enums.CourseStatus;
import com.xuecheng.content.model.entity.CourseAuditLog;
import com.xuecheng.content.model.entity.CourseBase;
import com.xuecheng.content.model.entity.CourseMarket;
import com.xuecheng.content.model.entity.CourseTeacher;
import com.xuecheng.content.model.entity.Teachplan;
import com.xuecheng.content.model.vo.CourseBaseVO;
import com.xuecheng.content.model.vo.CourseDetailVO;
import com.xuecheng.content.model.vo.PageResult;
import com.xuecheng.content.security.LoginUser;
import com.xuecheng.content.security.UserContext;
import com.xuecheng.content.model.enums.MessageType;
import com.xuecheng.content.service.CourseBaseService;
import com.xuecheng.content.service.MessageService;
import lombok.RequiredArgsConstructor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final CourseAuditLogMapper courseAuditLogMapper;
    private final MessageService messageService;

    @Override
    public PageResult<CourseBaseVO> pageQuery(PageParams pageParams, CourseQueryDTO queryDTO) {
        Long companyId = CompanyContext.getCompanyId();
        LambdaQueryWrapper<CourseBase> wrapper = new LambdaQueryWrapper<>();
        if (companyId != null) {
            wrapper.eq(CourseBase::getCompanyId, companyId);
        }

        if (queryDTO != null) {
            if (StringUtils.hasText(queryDTO.getCourseName())) {
                wrapper.like(CourseBase::getName, queryDTO.getCourseName());
            }
            if (StringUtils.hasText(queryDTO.getCourseStatus())) {
                if (!CourseStatus.isValid(queryDTO.getCourseStatus())) {
                    throw new BusinessException(400, "课程状态必须为000/100/111/112");
                }
                wrapper.eq(CourseBase::getCourseStatus, queryDTO.getCourseStatus());
            }
            if (StringUtils.hasText(queryDTO.getMt())) {
                wrapper.likeRight(CourseBase::getMt, queryDTO.getMt());
            }
            if (StringUtils.hasText(queryDTO.getSt())) {
                wrapper.eq(CourseBase::getSt, queryDTO.getSt());
            }
        }

        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        Page<CourseBase> result = courseBaseMapper.selectPage(page, wrapper);

        List<CourseBaseVO> items = result.getRecords().stream()
                .map(this::toVO)
                .toList();

        return new PageResult<>(items, result.getTotal(), pageParams.getPageNo(), pageParams.getPageSize());
    }

    private static final int MAX_DESCRIPTION_LEN = 5000;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(CourseCreateDTO dto) {
        if (dto.getDescription() != null && dto.getDescription().length() > MAX_DESCRIPTION_LEN) {
            throw new BusinessException(400, "课程介绍不能超过5000字符");
        }
        Long companyId = CompanyContext.getCompanyId();
        if (companyId == null) {
            throw new BusinessException(400, "创建课程需指定机构，请提供 X-Company-Id");
        }
        CourseBase entity = new CourseBase();
        BeanUtils.copyProperties(dto, entity);
        entity.setCompanyId(companyId);
        entity.setCourseStatus(CourseStatus.DRAFT.getCode());
        entity.setIsDeleted(0);
        LocalDateTime now = LocalDateTime.now();
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        entity.setCreateBy(String.valueOf(companyId));
        entity.setUpdateBy(String.valueOf(companyId));
        validateAndSyncCoverPics(entity);
        courseBaseMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public CourseDetailVO getById(Long id) {
        Long companyId = CompanyContext.getCompanyId();
        LambdaQueryWrapper<CourseBase> wrapper = new LambdaQueryWrapper<CourseBase>().eq(CourseBase::getId, id);
        if (companyId != null) {
            wrapper.eq(CourseBase::getCompanyId, companyId);
        }
        CourseBase base = courseBaseMapper.selectOne(wrapper);
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
        if (dto.getDescription() != null && dto.getDescription().length() > MAX_DESCRIPTION_LEN) {
            throw new BusinessException(400, "课程介绍不能超过5000字符");
        }
        ensureCompanyOwns(id);
        CourseBase entity = new CourseBase();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(id);
        entity.setUpdateTime(LocalDateTime.now());
        entity.setUpdateBy(String.valueOf(CompanyContext.getCompanyId() != null ? CompanyContext.getCompanyId() : "0"));
        validateAndSyncCoverPics(entity);
        courseBaseMapper.updateById(entity);
    }

    @Override
    public void updatePic(Long id, String fileId) {
        ensureCompanyOwns(id);
        if (fileId == null || fileId.isBlank()) {
            throw new BusinessException(400, "fileId不能为空");
        }
        courseBaseMapper.update(null, new LambdaUpdateWrapper<CourseBase>()
                .eq(CourseBase::getId, id)
                .set(CourseBase::getPic, "media:" + fileId.trim())
                .set(CourseBase::getUpdateTime, LocalDateTime.now())
                .set(CourseBase::getUpdateBy, String.valueOf(CompanyContext.getCompanyId() != null ? CompanyContext.getCompanyId() : "0")));
    }

    @Override
    public void submit(Long id) {
        ensureCompanyOwns(id);
        CourseBase base = courseBaseMapper.selectById(id);
        if (base == null) {
            throw new BusinessException(404, "课程不存在");
        }
        // 仅草稿(000)可提交为待审核(100)；112已下架禁止再提交
        if (CourseStatus.BANNED.getCode().equals(base.getCourseStatus())) {
            throw new BusinessException(400, "已下架课程禁止再次提交");
        }
        if (!CourseStatus.DRAFT.getCode().equals(base.getCourseStatus())) {
            throw new BusinessException(400, "仅草稿状态可提交审核");
        }
        courseBaseMapper.update(null, new LambdaUpdateWrapper<CourseBase>()
                .eq(CourseBase::getId, id)
                .set(CourseBase::getCourseStatus, CourseStatus.PENDING.getCode())
                .set(CourseBase::getUpdateTime, LocalDateTime.now())
                .set(CourseBase::getUpdateBy, String.valueOf(CompanyContext.getCompanyId() != null ? CompanyContext.getCompanyId() : "0")));
        saveSubmitAuditLog(base);
        sendSubmitMessage(base);
    }

    private void sendSubmitMessage(CourseBase base) {
        LoginUser user = UserContext.get();
        String fromName = user != null ? user.getUsername() : "系统";
        String title = "新课程待审核";
        String content = String.format("机构【%s】提交了新课程《%s》，请及时审核。",
                base.getCompanyName() != null ? base.getCompanyName() : "未知", base.getName());
        messageService.sendToRole(MessageType.COURSE_SUBMIT.getCode(), title, content,
                base.getId(), base.getName(), user != null ? user.getUserId() : null, fromName, "super_admin");
    }

    private void saveSubmitAuditLog(CourseBase base) {
        LoginUser user = UserContext.get();
        CourseAuditLog log = new CourseAuditLog();
        log.setCourseId(base.getId());
        log.setCourseName(base.getName());
        log.setCompanyId(base.getCompanyId());
        log.setCompanyName(base.getCompanyName());
        log.setAuditAction(AuditAction.SUBMIT.getCode());
        log.setAuditorId(user != null ? user.getUserId() : null);
        log.setAuditorName(user != null ? user.getUsername() : null);
        log.setOldStatus(CourseStatus.DRAFT.getCode());
        log.setNewStatus(CourseStatus.PENDING.getCode());
        log.setCreateTime(LocalDateTime.now());
        courseAuditLogMapper.insert(log);
    }

    @Override
    public void submitAndPublish(Long id) {
        ensureCompanyOwns(id);
        CourseBase base = courseBaseMapper.selectById(id);
        if (base == null) {
            throw new BusinessException(404, "课程不存在");
        }
        if (!CourseStatus.DRAFT.getCode().equals(base.getCourseStatus())) {
            throw new BusinessException(400, "仅草稿状态可提交并发布");
        }
        courseBaseMapper.update(null, new LambdaUpdateWrapper<CourseBase>()
                .eq(CourseBase::getId, id)
                .set(CourseBase::getCourseStatus, CourseStatus.PUBLISHED.getCode())
                .set(CourseBase::getUpdateTime, LocalDateTime.now())
                .set(CourseBase::getUpdateBy, String.valueOf(CompanyContext.getCompanyId() != null ? CompanyContext.getCompanyId() : "0")));
    }

    @Override
    public void publish(Long id) {
        ensureCompanyOwns(id);
        CourseBase base = courseBaseMapper.selectById(id);
        if (base == null) {
            throw new BusinessException(404, "课程不存在");
        }
        if (!CourseStatus.PENDING.getCode().equals(base.getCourseStatus())) {
            throw new BusinessException(400, "仅待审核状态可发布（审核通过）");
        }
        courseBaseMapper.update(null, new LambdaUpdateWrapper<CourseBase>()
                .eq(CourseBase::getId, id)
                .set(CourseBase::getCourseStatus, CourseStatus.PUBLISHED.getCode())
                .set(CourseBase::getUpdateTime, LocalDateTime.now())
                .set(CourseBase::getUpdateBy, String.valueOf(CompanyContext.getCompanyId() != null ? CompanyContext.getCompanyId() : "0")));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeById(Long id) {
        ensureCompanyOwns(id);
        courseBaseMapper.deleteById(id);
        courseMarketMapper.delete(new LambdaQueryWrapper<CourseMarket>().eq(CourseMarket::getId, id));
        LocalDateTime now = LocalDateTime.now();
        String by = CompanyContext.getCompanyId() != null ? String.valueOf(CompanyContext.getCompanyId()) : "0";
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
        if (companyId == null) {
            return; // SuperAdmin 可操作任意课程
        }
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

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int MAX_COVER_PICS = 3;

    private void validateAndSyncCoverPics(CourseBase entity) {
        String coverPics = entity.getCoverPics();
        Integer idx = entity.getDefaultCoverIndex();
        if (coverPics == null || coverPics.isBlank()) {
            return;
        }
        try {
            List<String> arr = OBJECT_MAPPER.readValue(coverPics, new TypeReference<List<String>>() {});
            if (arr.size() > MAX_COVER_PICS) {
                throw new BusinessException(400, "封面图最多3张");
            }
            if (idx != null && idx >= 0 && idx < arr.size()) {
                entity.setPic(arr.get(idx));
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(400, "封面图格式错误");
        }
    }
}
