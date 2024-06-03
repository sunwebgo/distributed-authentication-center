package com.mc.dynamic.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mc.common.constants.CacheConstants;
import com.mc.common.constants.CommonConstants;
import com.mc.common.constants.NumberConstants;
import com.mc.common.dubbo.UserServiceInterface;
import com.mc.common.entity.response.ResponseResult;
import com.mc.common.entity.table.Dynamic;
import com.mc.common.entity.to.dynamic.DynamicCacheDataTO;
import com.mc.common.entity.to.user.UserTO;
import com.mc.common.entity.vo.dynamic.DynamicVO;
import com.mc.common.entity.vo.page.PageVO;
import com.mc.common.enums.Http;
import com.mc.common.utils.BeanCopyUtils;
import com.mc.common.utils.RedisUtil;
import com.mc.dynamic.mapper.CollectMapper;
import com.mc.dynamic.mapper.DynamicMapper;
import com.mc.dynamic.service.CollectService;
import com.mc.dynamic.service.DynamicService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class CollectServiceImpl implements CollectService {

    @DubboReference(timeout = 1000)
    private UserServiceInterface userServiceInterface;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private CollectMapper collectMapper;

    @Resource
    private DynamicService dynamicService;

    @Resource
    private DynamicMapper dynamicMapper;

    /**
     * 收藏（点赞）动态 / 取消收藏（取消点赞）动态
     *
     * @param uId
     * @param dynamicId
     * @return {@link ResponseResult}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult collectDynamic(Long uId, Long dynamicId) {
        // 判断用户是否存在
        userServiceInterface.userIsExist(uId).whenCompleteAsync((result, throwable) -> {
            if (!result.getCode().equals(CommonConstants.SUCCESS_CODE) || ObjectUtils.isEmpty(result.getData())) {
                throw new RuntimeException(result.getMessage());
            }
        }, threadPoolExecutor);
        // 判断动态是否存在
        List<DynamicCacheDataTO> dynamicCacheDataTO = dynamicMapper.getDynamicCacheData(dynamicId);
        if (dynamicCacheDataTO.isEmpty()) {
            return ResponseResult.error(Http.DYNAMIC_NOT_EXIST.getMessage());
        }
        // 获取到动态信息
        DynamicCacheDataTO dynamic = dynamicCacheDataTO.get(NumberConstants.ZERO);
        try {
            // 判断当前用户是否已经点赞
            Boolean cacheResult = RedisUtil.setIsMember(CacheConstants.DYNAMIC_LIKE + dynamicId, uId.toString());
            if (Boolean.FALSE.equals(cacheResult)) { // 用户未点赞，新增点赞
                // 向set结构中添加用户id
                RedisUtil.setAdd(CacheConstants.DYNAMIC_LIKE + dynamicId, uId.toString());
                // 用户收藏表添加收藏动态
                Boolean collectResult = collectMapper.collect(uId, CommonConstants.COLLECT_DYNAMIC, dynamicId);
                if (Boolean.FALSE.equals(collectResult)) {
                    throw new RuntimeException(Http.COLLECT_FAIL.getMessage());
                }
                CompletableFuture<Double> future = getLikeCount(dynamic, NumberConstants.ATOMIC_DOUBLE);
                int likeCount = 0;
                if (ObjectUtils.isNotEmpty(future.get())) {
                    likeCount = future.get().intValue();
                }
                // 将最新的点赞数返回
                return ResponseResult.success(Http.LIKE_SUCCESS.getMessage(), likeCount);
            } else { // 用户已经点赞，取消点赞
                // 从set结构中移除用户id
                RedisUtil.setRemove(CacheConstants.DYNAMIC_LIKE + dynamicId, uId.toString());
                // 用户收藏表取消点赞动态
                Boolean cancelResult = collectMapper.cancelCollect(uId, CommonConstants.COLLECT_DYNAMIC, dynamicId);
                if (Boolean.FALSE.equals(cancelResult)) {
                    throw new RuntimeException(Http.CANCEL_LIKE_FAIL.getMessage());
                }
                CompletableFuture<Double> future = getLikeCount(dynamic, NumberConstants.ATOMIC_MINUS_DOUBLE);
                int likeCount = 0;
                if (ObjectUtils.isNotEmpty(future.get())) {
                    likeCount = future.get().intValue();
                }
                return ResponseResult.success(Http.CANCEL_LIKE_SUCCESS.getMessage(), likeCount);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 更新缓存中动态点赞数
     *
     * @param dynamic
     * @return {@link Double}
     */
    private CompletableFuture<Double> getLikeCount(DynamicCacheDataTO dynamic, Double number) {
        // 更新缓存中动态点赞数
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Double score = RedisUtil.zSetScore(CacheConstants.DYNAMIC_INFO, objectMapper.writeValueAsString(dynamic));
            if (ObjectUtils.isEmpty(score)) { // 动态数据为空，重建缓存
                return dynamicService.rebuildDynamicCacheInfo().thenApplyAsync((result) -> {
                    if (!result.getCode().equals(CommonConstants.SUCCESS_CODE)) {
                        throw new RuntimeException(result.getMessage());
                    }
                    try {
                        return RedisUtil.zSetIncrementScore(CacheConstants.DYNAMIC_INFO, objectMapper.writeValueAsString(dynamic), number);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }, threadPoolExecutor);
            } else {
                return CompletableFuture.completedFuture(RedisUtil.zSetIncrementScore(CacheConstants.DYNAMIC_INFO, objectMapper.writeValueAsString(dynamic), number));
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 查询用户动态收藏列表
     *
     * @param page
     * @param size
     * @param uId
     * @return {@link ResponseResult}<{@link PageVO}>
     */
    @Override
    public ResponseResult<PageVO> collectList(Integer page, Integer size, Long uId) {
        int start = (page - 1) * size;
        PageVO pageVO = new PageVO();
        // 判断用户是否存在
        CompletableFuture<ResponseResult<UserTO>> response = userServiceInterface.userIsExist(uId).whenCompleteAsync((result, throwable) -> {
            if (!result.getCode().equals(CommonConstants.SUCCESS_CODE) || ObjectUtils.isEmpty(result.getData())) {
                throw new RuntimeException(result.getMessage());
            }
        }, threadPoolExecutor);
        // 查询用户收藏动态的总数
        Integer total = collectMapper.collectCount(uId, CommonConstants.COLLECT_DYNAMIC);
        pageVO.setTotal(total);
        pageVO.setTotalPage(total % size == 0 ? total / size : total / size + 1);
        // 查询用户收藏动态列表
        List<Long> collectList = collectMapper.collectList(start, size, uId, CommonConstants.COLLECT_DYNAMIC);
        if (collectList.isEmpty()) {
            return ResponseResult.success();
        }
        // 查询动态信息
        List<Dynamic> dynamicList = dynamicMapper.getDynamicByIds(collectList);
        if (dynamicList.isEmpty()) {
            return ResponseResult.success();
        }
        List<DynamicVO> dynamicVOList = BeanCopyUtils.copyBeanList(dynamicList, DynamicVO.class);
//         判断用户是否点赞动态
        dynamicVOList.stream().forEach(dynamicVO -> {
            Boolean cacheResult = RedisUtil.setIsMember(CacheConstants.DYNAMIC_LIKE + dynamicVO.getId(), uId.toString());
            if (Boolean.TRUE.equals(cacheResult)) {
                dynamicVO.setIsLike(Boolean.TRUE);
            }
        });
        pageVO.setList(dynamicVOList);
        return ResponseResult.success(pageVO);
    }
}
