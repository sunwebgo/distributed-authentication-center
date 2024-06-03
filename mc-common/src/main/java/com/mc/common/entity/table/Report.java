package com.mc.common.entity.table;


import java.io.Serializable;

import lombok.*;

/**
 * 举报表
 *
 * @author Xu huaiang
 * @TableName report
 * @date 2024/02/03
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Report implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 举报id
     */
    private Long id;

    /**
     * 举报动态id
     */
    private Long dynamicId;

    /**
     * 举报原因
     */
    private String reason;


}
