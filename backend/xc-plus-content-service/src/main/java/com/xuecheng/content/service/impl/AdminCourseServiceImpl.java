package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xuecheng.content.exception.BusinessException;
import com.xuecheng.content.mapper.CourseAuditLogMapper;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.enums.AuditAction;
import com.xuecheng.content.model.enums.CourseStatus;
import com.xuecheng.content.model.entity.CourseAuditLog;
import com.xuecheng.content.model.entity.CourseBase;
import com.xuecheng.content.model.entity.CourseMarket;
import com.xuecheng.content.model.vo.CourseBaseVO;
import com.xuecheng.content.model.vo.CourseDetailVO;
import com.xuecheng.content.security.LoginUser;
import com.xuecheng.content.security.UserContext;
import com.xuecheng.content.model.enums.MessageType;
import com.xuecheng.content.service.AdminCourseService;
import com.xuecheng.content.service.MessageService;
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
    private final CourseAuditLogMapper courseAuditLogMapper;
    private final MessageService messageService;

    @Override
    public List<CourseBaseVO> listPending() {
        List<CourseBase> list = courseBaseMapper.selectList(
                new LambdaQueryWrapper<CourseBase>()
                        .eq(CourseBase::getCourseStatus, CourseStatus.PENDING.getCode())
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
    public void audit(Long id, String action, String opinion) {
        CourseBase base = courseBaseMapper.selectById(id);
        if (base == null) {
            throw new BusinessException(404, "课程不存在");
        }
        if (!CourseStatus.PENDING.getCode().equals(base.getCourseStatus())) {
            throw new BusinessException(400, "仅待审核状态可审核");
        }
        String oldStatus = base.getCourseStatus();
        String newStatus;
        AuditAction auditAction;
        switch (action) {
            case "approve" -> {
                newStatus = CourseStatus.PUBLISHED.getCode();
                auditAction = AuditAction.APPROVE;
            }
            case "reject" -> {
                newStatus = CourseStatus.DRAFT.getCode();
                auditAction = AuditAction.REJECT;
            }
            case "ban" -> {
                newStatus = CourseStatus.BANNED.getCode();
                auditAction = AuditAction.BAN;
            }
            default -> throw new BusinessException(400, "审核动作必须为approve/reject/ban");
        }
        courseBaseMapper.update(null, new LambdaUpdateWrapper<CourseBase>()
                .eq(CourseBase::getId, id)
                .set(CourseBase::getCourseStatus, newStatus)
                .set(CourseBase::getUpdateTime, LocalDateTime.now()));
        saveAuditLog(base, auditAction, oldStatus, newStatus, opinion);
        sendAuditMessage(base, auditAction, opinion);
    }

    private void sendAuditMessage(CourseBase base, AuditAction action, String opinion) {
        LoginUser user = UserContext.get();
        String fromName = user != null ? user.getUsername() : "系统";
        String type;
        String title;
        String content;
        switch (action) {
            case APPROVE -> {
                type = MessageType.AUDIT_APPROVED.getCode();
                title = "课程审核通过";
                content = String.format("您的课程《%s》已审核通过，现已发布。", base.getName());
            }
            case REJECT -> {
                type = MessageType.AUDIT_REJECTED.getCode();
                title = "课程审核退回";
                content = String.format("您的课程《%s》已被退回。%s", base.getName(),
                        opinion != null && !opinion.isBlank() ? "原因：" + opinion : "");
            }
            case BAN -> {
                type = MessageType.AUDIT_BANNED.getCode();
                title = "课程已下架";
                content = String.format("您的课程《%s》已被封禁下架。%s", base.getName(),
                        opinion != null && !opinion.isBlank() ? "原因：" + opinion : "");
            }
            default -> { return; }
        }
        messageService.sendToCompanyTeachers(type, title, content, base.getId(), base.getName(),
                user != null ? user.getUserId() : null, fromName, base.getCompanyId());
    }

    private void saveAuditLog(CourseBase base, AuditAction action, String oldStatus, String newStatus, String opinion) {
        LoginUser user = UserContext.get();
        CourseAuditLog log = new CourseAuditLog();
        log.setCourseId(base.getId());
        log.setCourseName(base.getName());
        log.setCompanyId(base.getCompanyId());
        log.setCompanyName(base.getCompanyName());
        log.setAuditAction(action.getCode());
        log.setAuditOpinion(opinion);
        log.setAuditorId(user != null ? user.getUserId() : null);
        log.setAuditorName(user != null ? user.getUsername() : null);
        log.setOldStatus(oldStatus);
        log.setNewStatus(newStatus);
        log.setCreateTime(LocalDateTime.now());
        courseAuditLogMapper.insert(log);
    }

    private CourseBaseVO toVO(CourseBase entity) {
        CourseBaseVO vo = new CourseBaseVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
