package com.mc.common.entity.vo.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class UserMultipleInfoVO implements Serializable {
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
     * 手机号
     */
    private String phone;

    /**
     * 性别
     */
    private String gender;

    /**
     * 头像地址
     */
    private String avatarUrl;

    /**
     * 生日
     */
    private LocalDate birthday;

    /**
     * 个性签名
     */
    private String signature;

    /**
     * 粉丝数
     */
    private Long fansCount;

    /**
     * 关注数
     */
    private Long followCount;
}
