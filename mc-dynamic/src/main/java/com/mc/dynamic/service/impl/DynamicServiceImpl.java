package com.mc.dynamic.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mc.common.constants.CacheConstants;
import com.mc.common.constants.CommonConstants;
import com.mc.common.constants.MQConstants;
import com.mc.common.constants.NumberConstants;
import com.mc.common.dubbo.MusicServiceInterface;
import com.mc.common.dubbo.UserServiceInterface;
import com.mc.common.entity.dto.dynamic.DynamicDTO;
import com.mc.common.entity.elastic.DataChangInfo;
import com.mc.common.entity.table.Dynamic;
import com.mc.common.entity.response.ResponseResult;
import com.mc.common.entity.table.User;
import com.mc.common.entity.to.dynamic.DynamicCacheDataTO;
import com.mc.common.entity.to.music.MusicTO;
import com.mc.common.entity.to.user.UserTO;
import com.mc.common.entity.vo.dynamic.DynamicVO;
import com.mc.common.entity.vo.page.PageVO;
import com.mc.common.enums.DataChangType;
import com.mc.common.enums.Http;
import com.mc.common.enums.Table;
import com.mc.common.utils.BeanCopyUtils;
import com.mc.common.utils.RedisUtil;
import com.mc.common.utils.SnowFlakeUtil;
import com.mc.dynamic.mapper.DynamicMapper;
import com.mc.dynamic.service.DynamicService;
import com.mc.dynamic.utils.SensitiveReplaceUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class DynamicServiceImpl implements DynamicService {

    @Resource
    private DynamicMapper dynamicMapper;

    @DubboReference(timeout = 2000)
    private UserServiceInterface userServiceInterface;

    @DubboReference(timeout = 2000)
    private MusicServiceInterface musicServiceInterface;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private SensitiveReplaceUtil sensitiveReplaceUtil;

    /**
     * 获取到所有动态
     *
     * @return {@link CompletableFuture}<{@link ResponseResult}>
     */
    @Override
    public CompletableFuture<ResponseResult<List<Dynamic>>> getAllDynamic() {
        return CompletableFuture.supplyAsync(() -> {
            List<Dynamic> allDynamic = dynamicMapper.getAllDynamic();
            if (CollectionUtils.isEmpty(allDynamic)) {
                return ResponseResult.success();
            }
            return ResponseResult.success(allDynamic);
        });
    }

    /**
     * 分页查询动态列表
     *
     * @param page
     * @param size
     * @param uId
     * @param selfDynamic
     * @return {@link ResponseResult}<{@link PageVO}<{@link DynamicVO}>>
     */
    @Override
    public ResponseResult<PageVO<DynamicVO>> dynamicList(Integer page, Integer size, Long uId, Long selfDynamic) {
        int start = (page - 1) * size;
        // 判断是否是用户查询自己的动态
        if (ObjectUtils.isNotEmpty(selfDynamic) && !selfDynamic.equals(uId)) {
            return ResponseResult.error(Http.DYNAMIC_LIST_FAIL.getMessage());
        }
        // 判断用户是否登录
        User user = null;
        if (ObjectUtils.isNotEmpty(uId)) {
            // 判断用户是否存在
            CompletableFuture<ResponseResult<User>> response = userServiceInterface.getUser(uId).whenCompleteAsync((result, throwable) -> {
                if (!result.getCode().equals(CommonConstants.SUCCESS_CODE) || ObjectUtils.isEmpty(result.getData())) {
                    throw new RuntimeException(result.getMessage());
                }
            }, threadPoolExecutor);
            try {
                user = response.get().getData();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        // 查询动态总数
        Integer total = dynamicMapper.dynamicCount(selfDynamic);
        Integer totalPage = total / size;
        // 查询动态列表
        List<Dynamic> dynamicList = dynamicMapper.dynamicMapper(start, size, selfDynamic);
        PageVO<DynamicVO> pageVO = new PageVO<>();
        if (dynamicList.isEmpty()) {
            return ResponseResult.success();
        }
        List<DynamicCacheDataTO> dynamicCacheDataTOList = BeanCopyUtils.copyBeanList(dynamicList, DynamicCacheDataTO.class);
        ObjectMapper objectMapper = new ObjectMapper();
        // 查询动态最新的点赞数
        for (int i = 0; i < dynamicCacheDataTOList.size(); i++) {
            String dynamicStr;
            try {
                dynamicStr = objectMapper.writeValueAsString(dynamicCacheDataTOList.get(i));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            // 如果缓存为空，则重建缓存
            Set<ZSetOperations.TypedTuple<String>> cacheDynamic = RedisUtil.zSetReverseRangeWithScores(CacheConstants.DYNAMIC_INFO, start, (size * page) - 1);
            if (cacheDynamic.isEmpty()) {
                rebuildDynamicCacheInfo();
            }
            Double score = RedisUtil.zSetScore(CacheConstants.DYNAMIC_INFO, dynamicStr);
            if (ObjectUtils.isNotEmpty(score)) {
                dynamicList.get(i).setLikeCount(score.intValue());
            }
        }
        List<DynamicVO> dynamicVOList = BeanCopyUtils.copyBeanList(dynamicList, DynamicVO.class);
        // 如果用户登录，判断用户是否点赞动态
        if (ObjectUtils.isNotEmpty(user)) {
            // 判断用户是否点赞动态
            dynamicVOList.stream().forEach(dynamic -> {
                Boolean cacheResult = RedisUtil.setIsMember(CacheConstants.DYNAMIC_LIKE + dynamic.getId(), uId.toString());
                if (Boolean.TRUE.equals(cacheResult)) {
                    dynamic.setIsLike(Boolean.TRUE);
                }
            });
        }
        pageVO = new PageVO<>(total, totalPage, dynamicVOList);
        return ResponseResult.success(pageVO);
    }


    /**
     * 重建动态缓存信息
     */
    public CompletableFuture<ResponseResult> rebuildDynamicCacheInfo() {
        return CompletableFuture.supplyAsync(() -> {
            ObjectMapper objectMapper = new ObjectMapper();
            dynamicMapper.getDynamicCacheData(null).stream().forEach(dynamic -> {
                try {
                    RedisUtil.zSetAdd(CacheConstants.DYNAMIC_INFO, objectMapper.writeValueAsString(dynamic), dynamic.getLikeCount());
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(Http.DYNAMIC_INFO_INIT_FAIL.getMessage());
                }
            });
            return ResponseResult.success(Http.SUCCESS.getMessage());
        });
    }

    /**
     * 新增动态
     *
     * @param dynamicDTO
     * @return {@link ResponseResult}
     */
    @Override
    public ResponseResult addDynamic(DynamicDTO dynamicDTO) {
        Dynamic dynamic = new Dynamic();
        UserTO user = null;
        MusicTO music = null;
        // 判断用户是否存在
        if (ObjectUtils.isNotEmpty(dynamicDTO.getUId())) {
            CompletableFuture<ResponseResult<UserTO>> response = userServiceInterface.userIsExist(dynamicDTO.getUId()).whenCompleteAsync((result, throwable) -> {
                if (!result.getCode().equals(CommonConstants.SUCCESS_CODE) || ObjectUtils.isEmpty(result.getData())) {
                    throw new RuntimeException(result.getMessage());
                }
            }, threadPoolExecutor);
            try {
                user = response.get().getData();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            dynamic.setUId(user.getId());
            dynamic.setUName(user.getUsername());
            dynamic.setAvatarUrl(user.getAvatarUrl());
            dynamic.setPublishDate(new Date());
        }
        // 判断音乐是否存在
        if (ObjectUtils.isNotEmpty(dynamicDTO.getMusicId())) {
            CompletableFuture<ResponseResult<MusicTO>> response = musicServiceInterface.musicIsExist(dynamicDTO.getMusicId()).whenCompleteAsync((result, throwable) -> {
                if (!result.getCode().equals(CommonConstants.SUCCESS_CODE) || ObjectUtils.isEmpty(result.getData())) {
                    throw new RuntimeException(result.getMessage());
                }
            }, threadPoolExecutor);
            try {
                music = response.get().getData();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            dynamic.setMusicId(music.getId());
            dynamic.setMusicName(music.getName());
            dynamic.setMusicAuthor(music.getAuthor());
            dynamic.setMusicCoverUrl(music.getCoverUrl());
            dynamic.setMusicUrl(music.getMusicUrl());
        }
        // 设置动态内容
        dynamic.setContent(dynamicDTO.getContent());
        // 判断图片是否为空
        if (StringUtils.isNotBlank(dynamicDTO.getImgUrls())) {
            dynamic.setImgUrls(dynamicDTO.getImgUrls());
        }
        // 雪花算法生成id
        dynamic.setId(SnowFlakeUtil.getNextId());
        // 动态内容敏感词过滤
        dynamic.setContent(sensitiveReplaceUtil.replaceSensitiveWord(
                dynamic.getContent(), SensitiveReplaceUtil.minMatchType));
        // 新增动态
        Boolean addResult = dynamicMapper.addDynamic(
                dynamic.getId(),
                dynamic.getUId(),
                dynamic.getUName(),
                dynamic.getAvatarUrl(),
                dynamic.getContent(),
                dynamic.getImgUrls(),
                dynamic.getMusicId(),
                dynamic.getMusicName(),
                dynamic.getMusicAuthor(),
                dynamic.getMusicCoverUrl(),
                dynamic.getMusicUrl());
        if (Boolean.FALSE.equals(addResult)) {
            return ResponseResult.error(Http.PUBLISH_DYNAMIC_FAIL.getMessage());
        }
        // 缓存新增动态
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            RedisUtil.zSetAdd(
                    CacheConstants.DYNAMIC_INFO,
                    objectMapper.writeValueAsString(BeanCopyUtils.copyBean(dynamic, DynamicCacheDataTO.class)),
                    NumberConstants.ZERO);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        // 向消息队列中写入新增动态数据消息
        List<Dynamic> dynamics = new ArrayList<>();
        dynamics.add(dynamic);
        rabbitTemplate.convertAndSend(
                MQConstants.DATA_SYNC_EXCHANGE,
                MQConstants.DYNAMIC_DATA_INSERT_KEY,
                new DataChangInfo(Table.mc_dynamic, DataChangType.INSERT, dynamics) {
                });
        return ResponseResult.success();
    }

    /**
     * 判断动态是否存在
     *
     * @param dynamicId
     * @return {@link Boolean}
     */
    public Boolean dynamicIsExist(Long dynamicId) {
        return dynamicMapper.dynamicIsExist(dynamicId);
    }

    /**
     * 删除动态
     *
     * @param uId
     * @param dynamicId
     * @return {@link ResponseResult}
     */
    @Override
    public ResponseResult deleteDynamic(Long uId, Long dynamicId) {
        // 判断动态是否存在
        List<DynamicCacheDataTO> dynamicCacheData = dynamicMapper.getDynamicCacheData(dynamicId);
        if (CollectionUtils.isEmpty(dynamicCacheData)) {
            return ResponseResult.error(Http.DYNAMIC_NOT_EXIST.getMessage());
        }
        // 删除动态
        dynamicMapper.deleteDynamic(uId, dynamicId);
        // 删除动态缓存
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            RedisUtil.zSetRemove(CacheConstants.DYNAMIC_INFO, objectMapper.writeValueAsString(dynamicCacheData.get(NumberConstants.ZERO)));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        List<Long> ids = new ArrayList<>();
        ids.add(dynamicId);
        // 向消息队列中写入删除动态数据消息
        rabbitTemplate.convertAndSend(
                MQConstants.DATA_SYNC_EXCHANGE,
                MQConstants.DYNAMIC_DATA_DELETE_KEY,
                new DataChangInfo(Table.mc_dynamic, DataChangType.DELETE, ids) {
                });
        return ResponseResult.success();
    }

    /**
     * 查询用户的动态数量
     *
     * @param uId
     * @return {@link CompletableFuture}<{@link ResponseResult}<{@link Integer}>>
     */
    @Override
    public CompletableFuture<ResponseResult<Integer>> dynamicCount(Long uId) {
        return CompletableFuture.supplyAsync(() -> {
            Integer dynamicCount = 0;
            dynamicCount = dynamicMapper.dynamicCount(uId);
            return ResponseResult.success(dynamicCount);
        });
    }

}
