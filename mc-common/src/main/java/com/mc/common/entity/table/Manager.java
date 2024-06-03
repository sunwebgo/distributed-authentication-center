package com.mc.common.entity.table;


import java.io.Serializable;
import lombok.*;

/**
 * 管理员表
 *
 * @author Xu huaiang
 * @TableName manager
 * @date 2024/02/03
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Manager implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 管理员id
     */
    private Integer id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 头像地址
     */
    private String avatarUrl;

    /**
     * 状态(0:禁用,1:启用)
     */
    private Integer status;

    /**
     * 是否删除(0:删除,1:未删除)
     */
    private Integer isDeleted;
}
