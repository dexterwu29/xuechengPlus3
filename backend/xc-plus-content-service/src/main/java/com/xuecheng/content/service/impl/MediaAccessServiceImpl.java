package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.mapper.CoursePurchaseMapper;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.MediaAccessResult;
import com.xuecheng.content.model.entity.CourseBase;
import com.xuecheng.content.model.entity.CourseMarket;
import com.xuecheng.content.model.entity.CoursePurchase;
import com.xuecheng.content.model.entity.Teachplan;
import com.xuecheng.content.model.entity.TeachplanMedia;
import com.xuecheng.content.model.enums.CourseStatus;
import com.xuecheng.content.service.MediaAccessService;
import com.xuecheng.content.service.MediaUploadService;
import com.xuecheng.content.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 媒资播放权限校验
 * 规则：课程111、登录、免费/试看/已购买
 */
@Service
@RequiredArgsConstructor
public class MediaAccessServiceImpl implements MediaAccessService {

    private static final String CHARGE_FREE = "201000";
    private static final String PREVIEW_YES = "1";

    private final TeachplanMediaMapper teachplanMediaMapper;
    private final CourseBaseMapper courseBaseMapper;
    private final CourseMarketMapper courseMarketMapper;
    private final TeachplanMapper teachplanMapper;
    private final CoursePurchaseMapper coursePurchaseMapper;
    private final MediaUploadService mediaUploadService;

    @Override
    public MediaAccessResult checkPlayAccess(String fileId, LoginUser user) {
        TeachplanMedia tm = teachplanMediaMapper.selectOne(
                new LambdaQueryWrapper<TeachplanMedia>()
                        .eq(TeachplanMedia::getFileId, fileId)
                        .last("LIMIT 1")
        );
        if (tm == null) {
            return MediaAccessResult.deny("course_not_published");
        }
        Long courseId = tm.getCourseId();
        Long teachplanId = tm.getTeachplanId();

        CourseBase course = courseBaseMapper.selectById(courseId);
        if (course == null || !CourseStatus.PUBLISHED.getCode().equals(course.getCourseStatus())) {
            return MediaAccessResult.deny("course_not_published");
        }

        if (user == null) {
            return MediaAccessResult.deny("not_logged_in");
        }

        CourseMarket market = courseMarketMapper.selectById(courseId);
        String charge = market != null ? market.getCharge() : null;
        if (CHARGE_FREE.equals(charge)) {
            return MediaAccessResult.allow(mediaUploadService.getFileInfo(fileId).getUrl());
        }

        Teachplan plan = teachplanMapper.selectById(teachplanId);
        if (plan != null && PREVIEW_YES.equals(plan.getIsPreviewEnabled())) {
            return MediaAccessResult.allow(mediaUploadService.getFileInfo(fileId).getUrl());
        }

        CoursePurchase purchase = coursePurchaseMapper.selectOne(
                new LambdaQueryWrapper<CoursePurchase>()
                        .eq(CoursePurchase::getUserId, user.getUserId())
                        .eq(CoursePurchase::getCourseId, courseId)
        );
        if (purchase != null) {
            return MediaAccessResult.allow(mediaUploadService.getFileInfo(fileId).getUrl());
        }

        return MediaAccessResult.deny("not_purchased");
    }
}
