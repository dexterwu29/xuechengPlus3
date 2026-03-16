package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.content.exception.BusinessException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.PageParams;
import com.xuecheng.content.model.enums.CourseStatus;
import com.xuecheng.content.model.entity.CourseBase;
import com.xuecheng.content.model.entity.CourseMarket;
import com.xuecheng.content.model.entity.Teachplan;
import com.xuecheng.content.model.entity.TeachplanMedia;
import com.xuecheng.content.model.vo.CourseBaseVO;
import com.xuecheng.content.model.vo.CourseDetailVO;
import com.xuecheng.content.model.vo.PageResult;
import com.xuecheng.content.model.vo.TeachplanMediaVO;
import com.xuecheng.content.model.vo.TeachplanTreeVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuecheng.content.service.MediaUploadService;
import com.xuecheng.content.service.PublicCourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 公开课程服务实现 - 仅查询已发布课程
 */
@Service
@RequiredArgsConstructor
public class PublicCourseServiceImpl implements PublicCourseService {

    private final CourseBaseMapper courseBaseMapper;
    private final CourseMarketMapper courseMarketMapper;
    private final TeachplanMapper teachplanMapper;
    private final TeachplanMediaMapper teachplanMediaMapper;
    private final MediaUploadService mediaUploadService;

    @Override
    public PageResult<CourseBaseVO> pagePublished(PageParams params, String courseName, String mt, String st) {
        LambdaQueryWrapper<CourseBase> wrapper = new LambdaQueryWrapper<CourseBase>()
                .eq(CourseBase::getCourseStatus, CourseStatus.PUBLISHED.getCode());
        if (StringUtils.hasText(courseName)) {
            wrapper.like(CourseBase::getName, courseName);
        }
        if (StringUtils.hasText(mt)) {
            wrapper.likeRight(CourseBase::getMt, mt);
        }
        if (StringUtils.hasText(st)) {
            wrapper.eq(CourseBase::getSt, st);
        }
        wrapper.orderByDesc(CourseBase::getUpdateTime);

        Page<CourseBase> page = new Page<>(params.getPageNo(), params.getPageSize());
        Page<CourseBase> result = courseBaseMapper.selectPage(page, wrapper);
        List<CourseBaseVO> items = result.getRecords().stream().map(this::toVO).toList();
        return new PageResult<>(items, result.getTotal(), params.getPageNo(), params.getPageSize());
    }

    @Override
    public CourseDetailVO getPublishedDetail(Long id) {
        CourseBase base = courseBaseMapper.selectOne(
                new LambdaQueryWrapper<CourseBase>()
                        .eq(CourseBase::getId, id)
                        .eq(CourseBase::getCourseStatus, CourseStatus.PUBLISHED.getCode())
        );
        if (base == null) {
            throw new BusinessException(404, "课程不存在或未发布");
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
        // 解析多封面并生成预签名URL
        String coverPics = base.getCoverPics();
        if (coverPics != null && !coverPics.isBlank()) {
            try {
                ObjectMapper om = new ObjectMapper();
                List<String> arr = om.readValue(coverPics, new TypeReference<List<String>>() {});
                List<String> urls = new ArrayList<>();
                for (String p : arr) {
                    if (p != null && p.startsWith("media:")) {
                        String fileId = p.substring(6);
                        try {
                            String url = mediaUploadService.getFileInfo(fileId).getUrl();
                            if (url != null) urls.add(url);
                        } catch (Exception ignored) {}
                    }
                }
                vo.setCoverPicUrls(urls);
            } catch (Exception ignored) {}
        } else if (base.getPic() != null && base.getPic().startsWith("media:")) {
            try {
                String fileId = base.getPic().substring(6);
                String url = mediaUploadService.getFileInfo(fileId).getUrl();
                if (url != null) vo.setCoverPicUrls(List.of(url));
            } catch (Exception ignored) {}
        }
        return vo;
    }

    @Override
    public List<TeachplanTreeVO> getPublishedTeachplans(Long courseId) {
        CourseBase base = courseBaseMapper.selectOne(
                new LambdaQueryWrapper<CourseBase>()
                        .eq(CourseBase::getId, courseId)
                        .eq(CourseBase::getCourseStatus, CourseStatus.PUBLISHED.getCode())
        );
        if (base == null) {
            throw new BusinessException(404, "课程不存在或未发布");
        }
        List<Teachplan> all = teachplanMapper.selectList(
                new LambdaQueryWrapper<Teachplan>()
                        .eq(Teachplan::getCourseId, courseId)
                        .orderByAsc(Teachplan::getOrderBy)
        );
        List<TeachplanMedia> mediaList = teachplanMediaMapper.selectList(
                new LambdaQueryWrapper<TeachplanMedia>()
                        .eq(TeachplanMedia::getCourseId, courseId)
                        .orderByAsc(TeachplanMedia::getOrderBy)
        );
        Map<Long, List<TeachplanMediaVO>> mediaMap = mediaList.stream()
                .map(this::toMediaVO)
                .collect(Collectors.groupingBy(TeachplanMediaVO::getTeachplanId));
        return buildTree(0L, all, mediaMap);
    }

    private List<TeachplanTreeVO> buildTree(Long parentId, List<Teachplan> all, Map<Long, List<TeachplanMediaVO>> mediaMap) {
        return all.stream()
                .filter(t -> parentId.equals(t.getParentId()))
                .map(t -> toTreeVO(t, mediaMap.getOrDefault(t.getId(), List.of())))
                .peek(vo -> vo.setChildren(buildTree(vo.getId(), all, mediaMap)))
                .collect(Collectors.toList());
    }

    private TeachplanTreeVO toTreeVO(Teachplan entity, List<TeachplanMediaVO> mediaList) {
        TeachplanTreeVO vo = new TeachplanTreeVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setMediaList(mediaList);
        vo.setTeachplanMedia(mediaList.isEmpty() ? null : mediaList.get(0));
        return vo;
    }

    private TeachplanMediaVO toMediaVO(TeachplanMedia entity) {
        TeachplanMediaVO vo = new TeachplanMediaVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    private CourseBaseVO toVO(CourseBase entity) {
        CourseBaseVO vo = new CourseBaseVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
