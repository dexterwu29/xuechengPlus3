package com.xuecheng.content.service;

import com.xuecheng.content.model.vo.CourseDetailVO;
import com.xuecheng.content.model.vo.CourseBaseVO;

import java.util.List;

/**
 * 管理员课程服务（SuperAdmin，跨机构）
 */
public interface AdminCourseService {

    /**
     * 待审核课程列表（audit_status=202003）
     */
    List<CourseBaseVO> listPending();

    /**
     * 获取课程详情（任意机构，用于审核预览）
     */
    CourseDetailVO getByIdForAudit(Long id);

    /**
     * 审核：通过(202004) 或 不通过(202001)
     */
    void audit(Long id, boolean approved);
}
