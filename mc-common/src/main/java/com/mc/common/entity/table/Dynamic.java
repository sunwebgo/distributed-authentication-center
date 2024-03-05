package com.mc.common.entity.table;

import java.io.Serializable;
import java.util.Date;
import lombok.*;

/**
 * 动态表
 *
 * @author Xu huaiang
 * @TableName dynamic
 * @date 2024/02/03
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Dynamic implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 动态id
     */
    private Long id;

    /**
     * 发布者id
     */
    private Long uId;

    /**
     * 发布者用户名
     */
    private String uName;

    /**
     * 发布者头像地址
     */
    private String avatarUrl;

    /**
     * 动态内容
     */
    private String content;

    /**
     * 动态类型(日常、音乐、流行、音乐人、最爱的音乐、最爱的歌手)
     */
    private String type;

    /**
     * 发布日期
     */
    private Date publishDate;

    /**
     * 音乐id
     */
    private Long musicId;

    /**
     * 点赞次数
     */
    private Long likeCount;

    /**
     * 收藏次数
     */
    private Long collectCount;

    /**
     * 评论次数
     */
    private Long commentCount;

    /**
     * 状态(0:禁用,1:启用)
     */
    private Integer status;

    /**
     * 是否删除(0:删除,1:未删除)
     */
    private Integer isDeleted;


}
