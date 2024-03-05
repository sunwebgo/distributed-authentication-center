package com.mc.common.entity.table;


import java.io.Serializable;
import java.util.Date;

import lombok.*;

/**
 * 评论表
 *
 * @author Xu huaiang
 * @TableName comment
 * @date 2024/02/03
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Comment implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 评论id
     */
    private Long id;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 父评论id
     */
    private Long parentId;

    /**
     * 回复评论id
     */
    private Long responseId;

    /**
     * 评论类型(1:音乐,2:动态)
     */
    private Integer commentType;

    /**
     * 评论对象id
     */
    private Long typeObjectId;

    /**
     * 评论者id
     */
    private Long uId;

    /**
     * 回复者id
     */
    private Long replyId;

    /**
     * 评论时间
     */
    private Date commentDate;

    /**
     * 状态(0:禁用,1:启用)
     */
    private Integer status;

    /**
     * 是否删除(0:删除,1:未删除)
     */
    private Integer isDeleted;


}
