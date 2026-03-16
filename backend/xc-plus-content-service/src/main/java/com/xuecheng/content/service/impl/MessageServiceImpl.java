package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.content.exception.BusinessException;
import com.xuecheng.content.mapper.SystemMessageMapper;
import com.xuecheng.content.mapper.XcUserMapper;
import com.xuecheng.content.model.entity.SystemMessage;
import com.xuecheng.content.model.entity.XcUser;
import com.xuecheng.content.model.vo.PageResult;
import com.xuecheng.content.model.vo.SystemMessageVO;
import com.xuecheng.content.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final SystemMessageMapper systemMessageMapper;
    private final XcUserMapper xcUserMapper;

    @Override
    public void sendToRole(String type, String title, String content, Long courseId, String courseName,
                          Long fromUserId, String fromUserName, String toRole) {
        SystemMessage msg = new SystemMessage();
        msg.setType(type);
        msg.setTitle(title);
        msg.setContent(content);
        msg.setCourseId(courseId);
        msg.setCourseName(courseName);
        msg.setFromUserId(fromUserId);
        msg.setFromUserName(fromUserName);
        msg.setToRole(toRole);
        msg.setIsRead(0);
        msg.setCreateTime(LocalDateTime.now());
        systemMessageMapper.insert(msg);
    }

    @Override
    public void sendToUser(String type, String title, String content, Long courseId, String courseName,
                          Long fromUserId, String fromUserName, Long toUserId) {
        SystemMessage msg = new SystemMessage();
        msg.setType(type);
        msg.setTitle(title);
        msg.setContent(content);
        msg.setCourseId(courseId);
        msg.setCourseName(courseName);
        msg.setFromUserId(fromUserId);
        msg.setFromUserName(fromUserName);
        msg.setToUserId(toUserId);
        msg.setIsRead(0);
        msg.setCreateTime(LocalDateTime.now());
        systemMessageMapper.insert(msg);
    }

    @Override
    public void sendToCompanyTeachers(String type, String title, String content, Long courseId, String courseName,
                                      Long fromUserId, String fromUserName, Long companyId) {
        List<XcUser> teachers = xcUserMapper.selectList(
                new LambdaQueryWrapper<XcUser>()
                        .eq(XcUser::getCompanyId, companyId)
                        .eq(XcUser::getRole, "teacher")
                        .eq(XcUser::getStatus, 1)
        );
        for (XcUser t : teachers) {
            sendToUser(type, title, content, courseId, courseName, fromUserId, fromUserName, t.getId());
        }
    }

    @Override
    public PageResult<SystemMessageVO> list(Long userId, String role, int pageNo, int pageSize) {
        LambdaQueryWrapper<SystemMessage> wrapper = new LambdaQueryWrapper<SystemMessage>();
        if ("super_admin".equals(role)) {
            wrapper.and(w -> w.eq(SystemMessage::getToUserId, userId)
                    .or(i -> i.eq(SystemMessage::getToRole, "super_admin").isNull(SystemMessage::getToUserId)));
        } else {
            wrapper.eq(SystemMessage::getToUserId, userId);
        }
        wrapper.orderByDesc(SystemMessage::getCreateTime);
        Page<SystemMessage> page = new Page<>(pageNo, pageSize);
        Page<SystemMessage> result = systemMessageMapper.selectPage(page, wrapper);
        List<SystemMessageVO> items = result.getRecords().stream().map(this::toVO).toList();
        return new PageResult<>(items, result.getTotal(), pageNo, pageSize);
    }

    @Override
    public long unreadCount(Long userId, String role) {
        LambdaQueryWrapper<SystemMessage> wrapper = new LambdaQueryWrapper<SystemMessage>()
                .eq(SystemMessage::getIsRead, 0);
        if ("super_admin".equals(role)) {
            wrapper.and(w -> w.eq(SystemMessage::getToUserId, userId)
                    .or(i -> i.eq(SystemMessage::getToRole, "super_admin").isNull(SystemMessage::getToUserId)));
        } else {
            wrapper.eq(SystemMessage::getToUserId, userId);
        }
        return systemMessageMapper.selectCount(wrapper);
    }

    @Override
    public void markAsRead(Long messageId, Long userId, String role) {
        SystemMessage msg = systemMessageMapper.selectById(messageId);
        if (msg == null) {
            throw new BusinessException(404, "消息不存在");
        }
        boolean canRead = userId.equals(msg.getToUserId())
                || ("super_admin".equals(msg.getToRole()) && msg.getToUserId() == null && "super_admin".equals(role));
        if (!canRead) {
            throw new BusinessException(403, "无权操作该消息");
        }
        systemMessageMapper.update(null, new LambdaUpdateWrapper<SystemMessage>()
                .eq(SystemMessage::getId, messageId)
                .set(SystemMessage::getIsRead, 1)
                .set(SystemMessage::getReadTime, LocalDateTime.now()));
    }

    private SystemMessageVO toVO(SystemMessage entity) {
        SystemMessageVO vo = new SystemMessageVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setIsRead(entity.getIsRead() != null && entity.getIsRead() == 1);
        return vo;
    }
}
