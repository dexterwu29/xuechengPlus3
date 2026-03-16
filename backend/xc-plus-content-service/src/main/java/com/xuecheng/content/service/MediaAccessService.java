package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.MediaAccessResult;
import com.xuecheng.content.security.LoginUser;

/**
 * 媒资播放权限校验服务
 * 规则：课程111、登录、免费/试看/已购买
 */
public interface MediaAccessService {

    /**
     * 检查用户是否有权播放指定媒资
     * @param fileId 媒资文件ID
     * @param user 当前用户（未登录为 null）
     * @return 权限结果，含 playUrl（当允许时）
     */
    MediaAccessResult checkPlayAccess(String fileId, LoginUser user);
}
