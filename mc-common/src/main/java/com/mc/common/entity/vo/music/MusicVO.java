package com.mc.common.entity.vo.music;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // 忽略未知属性
public class MusicVO implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 音乐id
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    /**
     * 音乐名
     */
    private String name;

    /**
     * 音乐地址
     */
    private String musicUrl;

    /**
     * 封面地址
     */
    private String coverUrl;

    /**
     * 作者
     */
    private String author;

    /**
     * 专辑
     */
    private String album;

    /**
     * 类型
     */
    private Integer type;

    /**
     * 点赞次数
     */
    private Integer likeCount;

    /**
     * 是否点赞
     */
    private Boolean isLike;
}
