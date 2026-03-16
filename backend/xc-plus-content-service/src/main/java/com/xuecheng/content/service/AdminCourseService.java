package com.xuecheng.content.service;

import com.xuecheng.content.model.vo.CourseDetailVO;
import com.xuecheng.content.model.vo.CourseBaseVO;

import java.util.List;

/**
 * 管理员课程服务（SuperAdmin，跨机构）
 */
public interface AdminCourseService {

    /**
     * 待审核课程列表（course_status=100）
     */
    List<CourseBaseVO> listPending();

    /**
     * 获取课程详情（任意机构，用于审核预览）
     */
    CourseDetailVO getByIdForAudit(Long id);

    /**
     * 审核：approve通过(100→111) / reject退回(100→000) / ban封禁(100→112)
     */
    void audit(Long id, String action, String opinion);
}
