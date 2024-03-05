package com.mc.dynamic.service.impl;

import com.mc.common.entity.table.Dynamic;
import com.mc.common.entity.response.ResponseResult;
import com.mc.dynamic.mapper.DynamicMapper;
import com.mc.dynamic.service.DynamicService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class DynamicServiceImpl implements DynamicService {

    @Resource
    private DynamicMapper dynamicMapper;

    @Override
    public ResponseResult getDynamicById(String id) {
        Dynamic dynamic = dynamicMapper.getDynamicById(id);
        return ResponseResult.success(dynamic);
    }
}
