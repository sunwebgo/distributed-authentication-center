package com.mc.dynamic.dubbo.provider;

import com.mc.common.dubbo.DynamicServiceInterface;
import com.mc.common.entity.response.ResponseResult;
import com.mc.common.entity.table.Dynamic;
import com.mc.common.enums.Http;
import com.mc.dynamic.service.DynamicService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@DubboService(version = "1.0.0")
@Component
public class DynamicServiceProvider implements DynamicServiceInterface {

    @Resource
    private DynamicService dynamicService;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public CompletableFuture<ResponseResult<List<Dynamic>>> getAllDynamic() {
        return dynamicService.getAllDynamic();
    }

    /**
     * 重建动态缓存信息
     * @return {@link CompletableFuture}<{@link ResponseResult}>
     */
    @Override
    public CompletableFuture<ResponseResult> rebuildDynamicCacheInfo() {
        return dynamicService.rebuildDynamicCacheInfo();
    }

    /**
     * 查询用户的动态数量
     *
     * @param uId
     * @return {@link CompletableFuture}<{@link ResponseResult}<{@link Integer}>>
     */
    @Override
    public CompletableFuture<ResponseResult<Integer>> dynamicCount(Long uId) {
        if (ObjectUtils.isEmpty(uId)) {
            return CompletableFuture.supplyAsync(() ->
                            ResponseResult.error(Http.USER_NOT_EXIST.getMessage()),
                    threadPoolExecutor);
        }
        return dynamicService.dynamicCount(uId);
    }
}
