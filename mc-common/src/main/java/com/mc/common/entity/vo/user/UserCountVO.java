package com.mc.common.entity.vo.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserCountVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 关注数
     */
    private Integer attentionCount;

    /**
     * 粉丝数
     */
    private Integer fansCount;

    /**
     * 动态数
     */
    private Integer dynamicCount;
}
