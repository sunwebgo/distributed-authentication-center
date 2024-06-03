package com.mc.common.entity.table;


import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * 评论表
 *
 * @author Xu huaiang
 * @TableName comment
 * @date 2024/02/03
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
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
     * 评论动态id
     */
    private Long commentObjId;

    /**
     * 评论者id
     */
    @JsonProperty("uId") // 反序列化时重命名，防止lombok和jackson冲突
    private Long uId;

    /**
     * 评论者昵称
     */
    @JsonProperty("uName") // 反序列化时重命名，防止lombok和jackson冲突
    private String uName;

    /**
     * 评论者头像
     */
    @JsonProperty("uAvatarUrl") // 反序列化时重命名，防止lombok和jackson冲突
    private String uAvatarUrl;

    /**
     * 父评论id
     */
    private Long rootId;

    /**
     * 被回复的评论id
     */
    private Long respCommentId;

    /**
     * 被评论者id
     */
    private Long byCommentUId;

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
