package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.TeachplanDTO;
import com.xuecheng.content.model.vo.TeachplanTreeVO;

import java.util.List;

/**
 * 课程计划服务
 */
public interface TeachplanService {

    List<TeachplanTreeVO> listTree(Long courseId);

    /** 管理员预览用，不校验机构 */
    List<TeachplanTreeVO> listTreeForAdmin(Long courseId);

    Long create(TeachplanDTO dto);

    void update(Long id, TeachplanDTO dto);

    void remove(Long id);

    void moveUp(Long id);

    void moveDown(Long id);
}
