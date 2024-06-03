package com.mc.common.entity.table;

import lombok.*;

/**
 * @author Xu huaiang
 * @TableName permission
 * @date 2024/02/15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Permission {
    private static final long serialVersionUID = 1L;
    /**
     * 权限id
     */
    private Integer id;

    /**
     * 权限路径
     */
    private String perPath;

    /**
     * 权限描述
     */
    private String perDesc;

    /**
     * 状态(0:禁用,1:启用)
     */
    private Integer status;

    /**
     * 是否删除(0:删除,1:未删除)
     */
    private Integer isDeleted;
}
