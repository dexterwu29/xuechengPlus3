package com.xuecheng.content.controller;

import com.xuecheng.content.model.vo.CourseDetailVO;
import com.xuecheng.content.model.vo.TeachplanTreeVO;
import com.xuecheng.content.service.CourseBaseService;
import com.xuecheng.content.service.PublicCourseService;
import com.xuecheng.content.service.TeachplanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 课程预览页 - 返回 Thymeleaf 渲染的 HTML
 */
@Controller
@RequestMapping
@RequiredArgsConstructor
public class CoursePreviewController {

    private final CourseBaseService courseBaseService;
    private final TeachplanService teachplanService;
    private final PublicCourseService publicCourseService;

    @GetMapping("/courses/{id}/preview")
    public String previewInternal(@PathVariable Long id, Model model) {
        CourseDetailVO course = courseBaseService.getById(id);
        List<TeachplanTreeVO> teachplans = teachplanService.listTree(id);
        model.addAttribute("course", course);
        model.addAttribute("teachplans", teachplans);
        model.addAttribute("isPublic", false);
        return "course-preview";
    }

    @GetMapping("/public/courses/{id}/preview")
    public String previewPublic(@PathVariable Long id, Model model) {
        CourseDetailVO course = publicCourseService.getPublishedDetail(id);
        List<TeachplanTreeVO> teachplans = publicCourseService.getPublishedTeachplans(id);
        model.addAttribute("course", course);
        model.addAttribute("teachplans", teachplans);
        model.addAttribute("isPublic", true);
        return "course-preview";
    }
}
