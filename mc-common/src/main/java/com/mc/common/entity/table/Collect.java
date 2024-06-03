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
@Data
@AllArgsConstructor
@NoArgsConstructor
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
    private Long objectId;

}
