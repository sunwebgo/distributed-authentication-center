package com.mc.common.entity.dto.dynamic;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DynamicDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 发布者id
     */
    @JsonProperty("uId") // 反序列化时重命名，防止lombok和jackson冲突
    @NotNull(message = "用户ID不能为空")
    private Long uId;

    /**
     * 动态内容
     */
    @NotBlank(message = "动态内容不能为空")
    private String content;

    /**
     * 动态图片地址
     */
    private String imgUrls;

    /**
     * 音乐id
     */
    private Long musicId;
}
