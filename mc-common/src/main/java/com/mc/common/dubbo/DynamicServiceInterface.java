package com.mc.common.dubbo;

import com.mc.common.entity.response.ResponseResult;
import com.mc.common.entity.table.Dynamic;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface DynamicServiceInterface {

    CompletableFuture<ResponseResult<List<Dynamic>>> getAllDynamic();

    /**
     * 重建动态缓存信息
     * @return {@link CompletableFuture}<{@link ResponseResult}>
     */
    CompletableFuture<ResponseResult> rebuildDynamicCacheInfo();

    /**
     * 查询用户的动态数量
     *
     * @param uId
     * @return {@link CompletableFuture}<{@link ResponseResult}<{@link Integer}>>
     */
    CompletableFuture<ResponseResult<Integer>> dynamicCount(Long uId);

}
