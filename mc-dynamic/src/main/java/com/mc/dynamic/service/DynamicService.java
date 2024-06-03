package com.mc.dynamic.service;

import com.mc.common.entity.dto.dynamic.DynamicDTO;
import com.mc.common.entity.response.ResponseResult;
import com.mc.common.entity.table.Dynamic;
import com.mc.common.entity.vo.dynamic.DynamicVO;
import com.mc.common.entity.vo.page.PageVO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface DynamicService {

    CompletableFuture<ResponseResult<List<Dynamic>>> getAllDynamic();

    ResponseResult<PageVO<DynamicVO>> dynamicList(Integer page, Integer size, Long uId, Long selfDynamic);

    CompletableFuture<ResponseResult> rebuildDynamicCacheInfo();

    ResponseResult addDynamic(DynamicDTO dynamicDTO);

    Boolean dynamicIsExist(Long dynamicId);

    ResponseResult deleteDynamic(Long uId, Long dynamicId);

    CompletableFuture<ResponseResult<Integer>> dynamicCount(Long uId);
}
