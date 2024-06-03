package com.mc.dynamic.controller;

import com.mc.common.constants.CommonConstants;
import com.mc.common.entity.response.ResponseResult;
import com.mc.common.entity.vo.page.PageVO;
import com.mc.common.enums.Http;
import com.mc.dynamic.service.CollectService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/dynamic/collect")
public class CollectController {

    @Resource
    private CollectService collectService;

    /**
     * 收藏（点赞）动态 / 取消收藏（取消点赞）动态
     *
     * @param uId
     * @param dynamicId
     * @return {@link ResponseResult}
     */
    @PostMapping("/like")
    public ResponseResult collectDynamic(@RequestParam Long uId, @RequestParam Long dynamicId) {
        if (ObjectUtils.isEmpty(uId)) {
            return ResponseResult.error(Http.NEED_LOGIN.getMessage());
        }
        if (ObjectUtils.isEmpty(dynamicId)) {
            return ResponseResult.error(Http.DYNAMIC_NOT_NULL.getMessage());
        }
        return collectService.collectDynamic(uId, dynamicId);
    }

    /**
     * 查询用户动态收藏列表
     *
     * @param page
     * @param size
     * @param uId
     * @return {@link ResponseResult}<{@link PageVO}>
     */
    @GetMapping("/list")
    public ResponseResult<PageVO> collectList(@RequestParam(value = "page") Integer page,
                                              @RequestParam(value = "size") Integer size,
                                              @RequestParam Long uId) {
        page = page == null || page <= 0 ? CommonConstants.DEFAULT_PAGE : page;
        size = size == null || size <= 0 ? CommonConstants.CUSTOMER_SIZE : size;
        if (ObjectUtils.isEmpty(uId)) {
            return ResponseResult.error(Http.NEED_LOGIN.getMessage());
        }
        return collectService.collectList(page, size, uId);
    }

}
