package com.mc.common.entity.table;


import java.io.Serializable;
import java.util.Date;
import lombok.*;

/**
 * 关注表
 *
 * @author Xu huaiang
 * @TableName attention
 * @date 2024/02/03
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Attention implements Serializable {
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
     * 被关注用户id
     */
    private Long byAttentionId;
}
