package com.mc.common.entity.to.music;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MusicTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 音乐id
     */
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
}
