package com.mc.common.entity.table;

import java.io.Serializable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 用户表
 *
 * @author Xu huaiang
 * @TableName user
 * @date 2024/02/03
 */
@Setter
@Getter
@ToString
@EqualsAndHashCode
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 用户id
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 性别
     */
    private String gender;

    /**
     * 头像地址
     */
    private String avatarUrl;

    /**
     * 粉丝数
     */
    private Long fansCount;

    /**
     * 关注数
     */
    private Long followCount;

    /**
     * 状态(0:禁用,1:启用)
     */
    private Integer status;

    /**
     * 是否删除(0:删除,1:未删除)
     */
    private Integer isDeleted;
}
