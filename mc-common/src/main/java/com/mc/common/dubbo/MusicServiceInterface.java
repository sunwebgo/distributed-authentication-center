package com.mc.common.dubbo;

import com.mc.common.entity.response.ResponseResult;
import com.mc.common.entity.table.Music;
import com.mc.common.entity.to.music.MusicTO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface MusicServiceInterface {

    /**
     * 获取到所有音乐
     * @return {@link CompletableFuture}<{@link ResponseResult}<{@link List}<{@link Music}>>>
     */
    CompletableFuture<ResponseResult<List<Music>>> getAllMusic();

    /**
     * 判断音乐是否存在
     * @param musicId
     * @return {@link CompletableFuture}<{@link ResponseResult}<{@link MusicTO}>>
     */
    CompletableFuture<ResponseResult<MusicTO>> musicIsExist(Long musicId);

    /**
     * 重建音乐缓存信息
     * @return {@link CompletableFuture}<{@link ResponseResult}>
     */
    CompletableFuture<ResponseResult> rebuildMusicCacheInfo();
}
