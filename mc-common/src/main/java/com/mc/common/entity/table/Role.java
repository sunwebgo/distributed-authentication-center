package com.mc.common.entity.table;

import lombok.*;

/**
 * @author Xu huaiang
 * @TableName role
 * @date 2024/02/15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Role {
    private static final long serialVersionUID = 1L;
    /**
     * 角色id
     */
    private Integer id;

    /**
     * 角色名
     */
    private String roleName;

    /**
     * 角色描述
     */
    private String roleDesc;

    /**
     * 状态(0:禁用,1:启用)
     */
    private Integer status;

    /**
     * 是否删除(0:删除,1:未删除)
     */
    private Integer isDeleted;
}
