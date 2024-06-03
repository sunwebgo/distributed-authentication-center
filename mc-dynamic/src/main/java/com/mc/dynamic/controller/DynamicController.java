package com.mc.dynamic.controller;

import com.mc.common.constants.CommonConstants;
import com.mc.common.entity.dto.dynamic.DynamicDTO;
import com.mc.common.entity.response.ResponseResult;
import com.mc.common.entity.vo.dynamic.DynamicVO;
import com.mc.common.entity.vo.page.PageVO;
import com.mc.common.enums.Http;
import com.mc.dynamic.service.DynamicService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/dynamic")
public class DynamicController {

    @Resource
    private DynamicService dynamicService;

    /**
     * 分页查询动态列表
     *
     * @param page
     * @param size
     * @return {@link ResponseResult}<{@link DynamicVO}<{@link DynamicVO}>>
     */
    @GetMapping("/list")
    public ResponseResult<PageVO<DynamicVO>> dynamicList(@RequestParam(value = "page") Integer page,
                                                         @RequestParam(value = "size") Integer size,
                                                         @RequestParam(value = "uId", required = false) Long uId,
                                                         @RequestParam(value = "selfDynamic", required = false) Long selfDynamic) {
        page = page == null || page <= 0 ? CommonConstants.DEFAULT_PAGE : page;
        size = size == null || size <= 0 ? CommonConstants.CUSTOMER_SIZE : size;
        return dynamicService.dynamicList(page, size, uId, selfDynamic);
    }

    /**
     * 新增动态
     *
     * @param dynamicDTO
     * @return {@link ResponseResult}
     */
    @PostMapping("/add")
    public ResponseResult addDynamic(@Valid @RequestBody DynamicDTO dynamicDTO) {
        return dynamicService.addDynamic(dynamicDTO);
    }

    /**
     * 删除动态
     * @param uId
     * @param dynamicId
     * @return {@link ResponseResult}
     */
    @DeleteMapping("/delete")
    public ResponseResult deleteDynamic(@RequestParam(value = "uId", required = false) Long uId,
                                        @RequestParam(value = "dynamicId", required = false) Long dynamicId) {
        if (ObjectUtils.isEmpty(uId)) {
            return ResponseResult.error(Http.USER_INFO_NOT_NULL.getMessage());
        }
        if (ObjectUtils.isEmpty(dynamicId)) {
            return ResponseResult.error(Http.DYNAMIC_ID_NOT_NULL.getMessage());
        }
        return dynamicService.deleteDynamic(uId, dynamicId);
    }
}
