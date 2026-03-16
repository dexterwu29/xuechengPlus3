package com.xuecheng.content.service;

import com.xuecheng.content.model.vo.PageResult;
import com.xuecheng.content.model.vo.SystemMessageVO;

/**
 * 站内信服务
 */
public interface MessageService {

    /**
     * 发送给指定角色（广播）
     */
    void sendToRole(String type, String title, String content, Long courseId, String courseName,
                    Long fromUserId, String fromUserName, String toRole);

    /**
     * 发送给指定用户
     */
    void sendToUser(String type, String title, String content, Long courseId, String courseName,
                    Long fromUserId, String fromUserName, Long toUserId);

    /**
     * 发送给机构下所有教师
     */
    void sendToCompanyTeachers(String type, String title, String content, Long courseId, String courseName,
                               Long fromUserId, String fromUserName, Long companyId);

    /**
     * 分页查询当前用户的消息（含角色广播）
     */
    PageResult<SystemMessageVO> list(Long userId, String role, int pageNo, int pageSize);

    /**
     * 未读数量（含角色广播）
     */
    long unreadCount(Long userId, String role);

    /**
     * 标记已读（role 用于校验角色广播消息的权限）
     */
    void markAsRead(Long messageId, Long userId, String role);
}
