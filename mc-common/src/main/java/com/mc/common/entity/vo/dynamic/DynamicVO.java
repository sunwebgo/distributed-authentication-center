package com.mc.common.entity.vo.dynamic;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Data
public class DynamicVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 动态id
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    /**
     * 发布者id
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("uId")
    private Long uId;

    /**
     * 发布者用户名
     */
    @JsonProperty("uName")
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
    @JsonFormat(shape = JsonFormat.Shape.STRING)
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
     * 是否点赞
     */
    private Boolean isLike;
}
