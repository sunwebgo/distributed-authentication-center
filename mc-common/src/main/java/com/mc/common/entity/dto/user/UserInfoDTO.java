package com.mc.common.entity.dto.user;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Data
public class UserInfoDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 用户id
     */
    @NotNull(message = "用户ID不能为空")
    private Long id;

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[\u4e00-\u9fa5a-zA-Z][\u4e00-\u9fa5a-zA-Z0-9]{1,14}$", message = "请输入2-15位以汉字或字母开头，仅包含汉字、字母和数字的用户名")
    private String username;

    /**
     * 个性签名
     */
    @Pattern(regexp = "^.{0,64}$", message = "个性签名不能超过50个字符")
    private String signature;

    /**
     * 性别
     */
    @NotBlank(message = "性别不能为空")
    @Pattern(regexp = "^[男女]$", message = "性别只能为男或女")
    private String gender;

    /**
     * 生日
     */
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "生日格式不正确")
    private String birthday;
}
