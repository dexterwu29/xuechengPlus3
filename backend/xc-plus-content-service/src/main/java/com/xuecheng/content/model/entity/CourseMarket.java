package com.xuecheng.content.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程营销信息实体
 */
@Data
@TableName("course_market")
public class CourseMarket {

    @TableId
    private Long id;
    private String charge;
    private Float price;
    @TableField("original_price")
    private Float originalPrice;
    private String qq;
    private String wechat;
    private String phone;
    @TableField("valid_days")
    private Integer validDays;
    @TableLogic
    private Integer isDeleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String createBy;
    private String updateBy;
}
