package com.xuecheng.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.content.model.entity.Teachplan;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 课程计划 Mapper
 */
public interface TeachplanMapper extends BaseMapper<Teachplan> {

    @Select("SELECT COALESCE(MAX(order_by), 0) FROM teachplan WHERE course_id = #{courseId} AND parent_id = #{parentId}")
    Integer selectMaxOrderBy(@Param("courseId") Long courseId, @Param("parentId") Long parentId);

    /** 将 order_by >= fromOrderBy 的兄弟节点后移 delta 位，用于插入到指定位置 */
    @Update("UPDATE teachplan SET order_by = order_by + #{delta} WHERE course_id = #{courseId} AND parent_id = #{parentId} AND order_by >= #{fromOrderBy}")
    int shiftOrderByFrom(@Param("courseId") Long courseId, @Param("parentId") Long parentId, @Param("fromOrderBy") int fromOrderBy, @Param("delta") int delta);
}
