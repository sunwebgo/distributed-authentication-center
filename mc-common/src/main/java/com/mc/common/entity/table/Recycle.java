package com.mc.common.entity.table;

import java.io.Serializable;
import java.util.Date;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 回收站表
 *
 * @author Xu huaiang
 * @TableName recycle
 * @date 2024/02/03
 */
@Setter
@Getter
@ToString
@EqualsAndHashCode
public class Recycle implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 编号
     */
    private Integer id;

    /**
     * 回收类型(1:音乐,2:动态,3:评论,4:用户,5:举报)
     */
    private Integer type;

    /**
     * 回收对象id
     */
    private Long typeId;

    /**
     * 删除时间
     */
    private Date deleteTime;

    /**
     * 剩余时间
     */
    private Date remainTime;


}
