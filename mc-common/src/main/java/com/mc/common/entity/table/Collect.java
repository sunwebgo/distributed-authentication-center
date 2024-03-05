package com.mc.common.entity.table;

import java.io.Serializable;
import java.util.Date;

import lombok.*;

/**
 * 收藏表
 *
 * @author Xu huaiang
 * @TableName collect
 * @date 2024/02/03
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Collect implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 收藏id
     */
    private Long id;

    /**
     * 收藏者id
     */
    private Long uId;

    /**
     * 收藏类型(1:音乐,2:动态)
     */
    private Integer type;

    /**
     * 收藏对象id
     */
    private Long typeObject;

    /**
     * 收藏时间
     */
    private Date collectDate;

    /**
     * 状态(0:禁用,1:启用)
     */
    private Integer status;

    /**
     * 是否删除(0:删除,1:未删除)
     */
    private Integer isDeleted;

}
