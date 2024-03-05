package com.mc.dynamic.controller;

import com.mc.common.entity.response.ResponseResult;
import com.mc.dynamic.service.DynamicService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/dynamic")
public class DynamicController {

    @Resource
    private DynamicService dynamicService;

    @GetMapping("/test")
    public String test() {
        return "test";
    }

    @GetMapping("/getDynamicById")
    public ResponseResult getDynamicById(@RequestParam("id") String id) {
        return dynamicService.getDynamicById(id);
    }
}
