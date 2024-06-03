package com.mc.common.entity.to.music;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 音乐缓存数据
 * @author Xu huaiang
 * @date 2024/03/26
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MusicCacheDataTO implements Serializable {
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

    /**
     * 专辑
     */
    private String album;

    /**
     * 类型(1:流行,2:摇滚,3:民谣,4:电子,5:轻音乐)'
     */
    private Integer type;

    /**
     * 点赞次数
     */
    private Integer likeCount;
}
