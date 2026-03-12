package com.xuecheng.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.content.model.entity.Teachplan;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 课程计划 Mapper
 */
public interface TeachplanMapper extends BaseMapper<Teachplan> {

    @Select("SELECT COALESCE(MAX(order_by), 0) FROM teachplan WHERE course_id = #{courseId} AND parent_id = #{parentId}")
    Integer selectMaxOrderBy(@Param("courseId") Long courseId, @Param("parentId") Long parentId);
}
