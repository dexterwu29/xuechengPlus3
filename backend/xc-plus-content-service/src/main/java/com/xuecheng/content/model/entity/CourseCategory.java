package com.xuecheng.content.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程分类实体（树形结构）
 */
@Data
@TableName("course_category")
public class CourseCategory {

    @TableId
    private String id;
    private String name;
    private String label;
    @TableField("parent_id")
    private String parentId;
    @TableField("is_visible")
    private Integer isVisible;
    @TableField("order_by")
    private Integer orderBy;
    @TableField("is_leaf")
    private Integer isLeaf;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String createBy;
    private String updateBy;
}
