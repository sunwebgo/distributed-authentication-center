package com.mc.common.entity.dto.dynamic;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 评论内容
     */
    @NotBlank(message = "评论内容不能为空")
    private String content;

    /**
     * 评论动态id
     */
    @NotNull(message = "评论对象ID不能为空")
    private Long commentObjId;

    /**
     * 评论者id
     */
    @JsonProperty("uId") // 反序列化时重命名，防止lombok和jackson冲突
    @NotNull(message = "用户ID不能为空")
    private Long uId;

    /**
     * 评论者昵称
     */
    @JsonProperty("uName") // 反序列化时重命名，防止lombok和jackson冲突
    @NotBlank(message = "用户昵称不能为空")
    @Pattern(regexp = "^[\u4e00-\u9fa5a-zA-Z][\u4e00-\u9fa5a-zA-Z0-9]{1,14}$", message = "用户昵称格式错误")
    private String uName;

    /**
     * 评论者头像
     */
    @JsonProperty("uAvatarUrl") // 反序列化时重命名，防止lombok和jackson冲突
    @NotBlank(message = "用户头像不能为空")
    @Pattern(regexp = "^(http|https)://.*$", message = "用户头像格式错误")
    private String uAvatarUrl;

    /**
     * 根评论id
     */
    private Long rootId;

    /**
     * 被回复的评论id
     */
    private Long respCommentId;

    /**
     * 被评论者id
     */
    @NotNull(message = "被评论者不能为空")
    private Long byCommentUId;

}
