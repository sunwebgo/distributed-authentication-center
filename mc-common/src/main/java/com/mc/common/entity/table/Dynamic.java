package com.mc.common.entity.table;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * 动态表
 *
 * @author Xu huaiang
 * @TableName dynamic
 * @date 2024/02/03
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Dynamic implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 动态id
     */
    private Long id;

    /**
     * 发布者id
     */
    @JsonProperty("uId") // 反序列化时重命名，防止lombok和jackson冲突
    private Long uId;

    /**
     * 发布者用户名
     */
    @JsonProperty("uName") // 反序列化时重命名，防止lombok和jackson冲突
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
     * 动态图片地址
     */
    private String imgUrls;

    /**
     * 发布日期
     */
    private Date publishDate;

    /**
     * 音乐id
     */
    private Long musicId;

    /**
     * 音乐名称
     */
    private String musicName;

    /**
     * 音乐作者
     */
    private String musicAuthor;

    /**
     * 音乐封面
     */
    private String musicCoverUrl;

    /**
     * 音乐地址
     */
    private String musicUrl;

    /**
     * 点赞次数
     */
    private Integer likeCount;

    /**
     * 收藏次数
     */
    private Integer collectCount;

    /**
     * 评论次数
     */
    private Integer commentCount;

    /**
     * 状态(0:禁用,1:启用)
     */
    private Integer status;

    /**
     * 是否删除(0:删除,1:未删除)
     */
    private Integer isDeleted;


}
