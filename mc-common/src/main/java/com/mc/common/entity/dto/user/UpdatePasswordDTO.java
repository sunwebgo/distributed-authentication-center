package com.mc.common.entity.dto.user;


import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
@Data
public class UpdatePasswordDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 用户id
     */
    @NotNull(message = "用户ID不能为空")
    private Long id;

    /**
     * 旧密码
     */
    @NotBlank(message = "旧密码不能为空")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9]{8,15}$", message = "旧密码必须包含字母和数字，且长度为8-15位")
    private String oldPassword;

    /**
     * 新密码
     */
    @NotBlank(message = "新密码不能为空")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9]{8,15}$", message = "新密码必须包含字母和数字，且长度为8-15位")
    private String newPassword;

    /**
     * 确认新密码
     */
    @NotBlank(message = "确认新密码不能为空")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9]{8,15}$", message = "确认新密码必须包含字母和数字，且长度为8-15位")
    private String reNewPassword;
}
