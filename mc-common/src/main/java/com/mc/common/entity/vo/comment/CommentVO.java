package com.mc.common.entity.vo.comment;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class CommentVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 评论id
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论动态id
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long commentObjId;

    /**
     * 评论者id
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("uId")
    private Long uId;

    /**
     * 评论者昵称
     */
    @JsonProperty("uName")
    private String uName;

    /**
     * 评论者头像
     */
    @JsonProperty("uAvatarUrl")
    private String uAvatarUrl;

    /**
     * 父评论id
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long rootId;

    /**
     * 被回复的评论id
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long respCommentId;

    /**
     * 评论时间
     */
    private Date commentDate;

    /**
     * 子评论
     */
    private List<CommentVO> children;

    /**
     * 被评论者id
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long byCommentUId;


    /**
     * 被评论的内容
     */
    private String byRespContent;

}
