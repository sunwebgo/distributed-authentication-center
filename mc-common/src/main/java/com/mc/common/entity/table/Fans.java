package com.mc.common.entity.table;


import java.io.Serializable;
import java.util.Date;
import lombok.*;

/**
 * 粉丝表
 *
 * @author Xu huaiang
 * @TableName fans
 * @date 2024/02/03
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Fans implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 编号id
     */
    private Integer id;

    /**
     * 用户id
     */
    private Long uId;

    /**
     * 粉丝id
     */
    private Long fansId;

    /**
     * 关注时间
     */
    private Date fansDate;

    /**
     * 状态(0:禁用,1:启用)
     */
    private Integer status;

    /**
     * 是否删除(0:删除,1:未删除)
     */
    private Integer isDeleted;
}
