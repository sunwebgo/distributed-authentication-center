package com.mc.common.entity.table;


import java.io.Serializable;
import java.util.Date;

import lombok.*;

/**
 * 音乐表
 *
 * @author Xu huaiang
 * @TableName music
 * @date 2024/02/03
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Music implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 音乐id
     */
    private Long id;

    /**
     * 音乐名
     */
    private String name;

    /**
     * 音乐地址
     */
    private String musicUrl;

    /**
     * 封面地址
     */
    private String coverUrl;

    /**
     * 作者
     */
    private String author;

    /**
     * 专辑
     */
    private String album;

    /**
     * 歌词
     */
    private String lyric;

    /**
     * 发布日期
     */
    private Date publishDate;

    /**
     * 上传日期
     */
    private Date pushDate;

    /**
     * 类型
     */
    private Integer type;

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
