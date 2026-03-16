package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuecheng.content.config.CompanyContext;
import com.xuecheng.content.exception.BusinessException;
import com.xuecheng.content.mapper.PlatformArchiveMapper;
import com.xuecheng.content.model.dto.ArchiveDTO;
import com.xuecheng.content.model.dto.PageParams;
import com.xuecheng.content.model.entity.PlatformArchive;
import com.xuecheng.content.model.vo.ArchiveVO;
import com.xuecheng.content.model.vo.PageResult;
import com.xuecheng.content.service.ArchiveService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 平台备案服务实现
 */
@Service
@RequiredArgsConstructor
public class ArchiveServiceImpl implements ArchiveService {

    private static final ObjectMapper OM = new ObjectMapper();

    private final PlatformArchiveMapper archiveMapper;

    @Override
    public PageResult<ArchiveVO> pageQuery(PageParams params, String archiveType, String status) {
        Long companyId = CompanyContext.getCompanyId();
        LambdaQueryWrapper<PlatformArchive> wrapper = new LambdaQueryWrapper<>();
        if (companyId != null) {
            wrapper.eq(PlatformArchive::getCompanyId, companyId);
        }
        if (StringUtils.hasText(archiveType)) {
            wrapper.eq(PlatformArchive::getArchiveType, archiveType);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(PlatformArchive::getStatus, status);
        }
        wrapper.orderByDesc(PlatformArchive::getUpdateTime);

        Page<PlatformArchive> page = new Page<>(params.getPageNo(), params.getPageSize());
        Page<PlatformArchive> result = archiveMapper.selectPage(page, wrapper);
        List<ArchiveVO> items = result.getRecords().stream().map(this::toVO).toList();
        return new PageResult<>(items, result.getTotal(), params.getPageNo(), params.getPageSize());
    }

    @Override
    public ArchiveVO getById(Long id) {
        PlatformArchive entity = archiveMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "备案不存在");
        }
        ensureCompanyOwns(entity);
        return toVO(entity);
    }

    @Override
    public Long create(ArchiveDTO dto) {
        Long companyId = CompanyContext.getCompanyId();
        if (companyId == null) {
            throw new BusinessException(400, "请提供 X-Company-Id");
        }
        if (!StringUtils.hasText(dto.getArchiveType())) {
            throw new BusinessException(400, "类型不能为空");
        }

        PlatformArchive entity = new PlatformArchive();
        entity.setCompanyId(companyId);
        entity.setArchiveType(dto.getArchiveType());
        entity.setName(dto.getName());
        entity.setContactInfo(toContactJson(dto));
        entity.setStatus("pending");
        entity.setIsDeleted(0);
        LocalDateTime now = LocalDateTime.now();
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        entity.setCreateBy(String.valueOf(companyId));
        entity.setUpdateBy(String.valueOf(companyId));
        archiveMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void updateById(Long id, ArchiveDTO dto) {
        PlatformArchive entity = archiveMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "备案不存在");
        }
        ensureCompanyOwns(entity);
        entity.setName(dto.getName());
        entity.setContactInfo(toContactJson(dto));
        entity.setUpdateTime(LocalDateTime.now());
        entity.setUpdateBy(String.valueOf(CompanyContext.getCompanyId() != null ? CompanyContext.getCompanyId() : "0"));
        archiveMapper.updateById(entity);
    }

    @Override
    public void removeById(Long id) {
        PlatformArchive entity = archiveMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "备案不存在");
        }
        ensureCompanyOwns(entity);
        archiveMapper.deleteById(id);
    }

    @Override
    public void approve(Long id) {
        PlatformArchive entity = archiveMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "备案不存在");
        }
        entity.setStatus("approved");
        entity.setUpdateTime(LocalDateTime.now());
        entity.setUpdateBy(String.valueOf(CompanyContext.getCompanyId() != null ? CompanyContext.getCompanyId() : "0"));
        archiveMapper.updateById(entity);
    }

    @Override
    public void reject(Long id) {
        PlatformArchive entity = archiveMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "备案不存在");
        }
        entity.setStatus("rejected");
        entity.setUpdateTime(LocalDateTime.now());
        entity.setUpdateBy(String.valueOf(CompanyContext.getCompanyId() != null ? CompanyContext.getCompanyId() : "0"));
        archiveMapper.updateById(entity);
    }

    private void ensureCompanyOwns(PlatformArchive entity) {
        Long companyId = CompanyContext.getCompanyId();
        if (companyId == null) return;
        if (!companyId.equals(entity.getCompanyId())) {
            throw new BusinessException(403, "无权操作该备案");
        }
    }

    private String toContactJson(ArchiveDTO dto) {
        Map<String, String> m = new HashMap<>();
        if (dto.getPhone() != null) m.put("phone", dto.getPhone());
        if (dto.getWechat() != null) m.put("wechat", dto.getWechat());
        if (dto.getQq() != null) m.put("qq", dto.getQq());
        if (dto.getIdCard() != null) m.put("idCard", dto.getIdCard());
        if (dto.getLicenseNo() != null) m.put("licenseNo", dto.getLicenseNo());
        try {
            return OM.writeValueAsString(m);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private ArchiveVO toVO(PlatformArchive entity) {
        ArchiveVO vo = new ArchiveVO();
        vo.setId(entity.getId());
        vo.setCompanyId(entity.getCompanyId());
        vo.setArchiveType(entity.getArchiveType());
        vo.setName(entity.getName());
        vo.setStatus(entity.getStatus());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        if (entity.getContactInfo() != null && !entity.getContactInfo().isBlank()) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, String> m = OM.readValue(entity.getContactInfo(), Map.class);
                vo.setPhone(m.get("phone"));
                vo.setWechat(m.get("wechat"));
                vo.setQq(m.get("qq"));
                vo.setIdCard(m.get("idCard"));
                vo.setLicenseNo(m.get("licenseNo"));
            } catch (Exception ignored) {}
        }
        return vo;
    }
}
