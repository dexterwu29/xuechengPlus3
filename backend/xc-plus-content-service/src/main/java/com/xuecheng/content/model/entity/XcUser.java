package com.xuecheng.content.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@TableName("xc_user")
public class XcUser {

    @TableId
    private Long id;
    private String username;
    @TableField("password_hash")
    private String passwordHash;
    @TableField("real_name")
    private String realName;
    private String role;  // super_admin, teacher, visitor
    @TableField("company_id")
    private Long companyId;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
