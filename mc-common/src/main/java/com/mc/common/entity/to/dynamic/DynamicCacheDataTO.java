package com.mc.common.entity.to.dynamic;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DynamicCacheDataTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 动态id
     */
    private Long id;

    /**
     * 动态内容
     */
    private String content;

    /**
     * 发布者id
     */
    @JsonProperty("uId")
    private Long uId;

    /**
     * 发布者用户名
     */
    @JsonProperty("uName")
    private String uName;

    /**
     * 点赞次数
     */
    private Integer likeCount;
}
