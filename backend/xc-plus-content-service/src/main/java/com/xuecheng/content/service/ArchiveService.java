package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.ArchiveDTO;
import com.xuecheng.content.model.dto.PageParams;
import com.xuecheng.content.model.vo.ArchiveVO;
import com.xuecheng.content.model.vo.PageResult;

/**
 * 平台备案服务
 */
public interface ArchiveService {

    PageResult<ArchiveVO> pageQuery(PageParams params, String archiveType, String status);

    ArchiveVO getById(Long id);

    Long create(ArchiveDTO dto);

    void updateById(Long id, ArchiveDTO dto);

    void removeById(Long id);

    void approve(Long id);

    void reject(Long id);
}
