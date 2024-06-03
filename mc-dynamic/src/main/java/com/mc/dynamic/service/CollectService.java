package com.mc.dynamic.service;

import com.mc.common.entity.response.ResponseResult;
import com.mc.common.entity.vo.page.PageVO;

public interface CollectService {
    ResponseResult collectDynamic(Long uId, Long dynamicId);

    ResponseResult<PageVO> collectList(Integer page, Integer size, Long uId);
}
