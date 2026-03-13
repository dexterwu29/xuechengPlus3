package com.xuecheng.content.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程购买记录
 */
@Data
@TableName("course_purchase")
public class CoursePurchase {

    @TableId
    private Long id;
    @TableField("user_id")
    private Long userId;
    @TableField("course_id")
    private Long courseId;
    @TableField("order_id")
    private String orderId;
    private LocalDateTime createTime;
}
