package com.mc.common.entity.vo.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;

@Data
public class UserAttentionVO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 用户id
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 头像地址
     */
    private String avatarUrl;

    /**
     * 个性签名
     */
    private String signature;

    /**
     * 是否关注
     */
    private Boolean isAttention;
}
