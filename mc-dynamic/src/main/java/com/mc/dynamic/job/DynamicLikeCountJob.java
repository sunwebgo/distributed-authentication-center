package com.mc.dynamic.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mc.common.constants.CacheConstants;
import com.mc.common.constants.NumberConstants;
import com.mc.common.entity.table.Dynamic;
import com.mc.common.utils.RedisUtil;
import com.mc.dynamic.mapper.DynamicMapper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;

@Component
public class DynamicLikeCountJob {

    @Resource
    private DynamicMapper dynamicMapper;

    /**
     * 音乐点赞数定时回库
     */
    @XxlJob("dynamicLikeCountJobHandler")
    public void dynamicLikeCountJobHandler() {
        // 从缓存中查询音乐点赞数
        Set<ZSetOperations.TypedTuple<String>> cacheDynamic = RedisUtil.zSetReverseRangeWithScores(CacheConstants.DYNAMIC_INFO, NumberConstants.ZERO, NumberConstants.MINUS_ONE);
        if (cacheDynamic.isEmpty()) {
            return;
        }
        System.out.println("定时任务执行：更新动态点赞数量");
        ObjectMapper objectMapper = new ObjectMapper();
        cacheDynamic.stream().forEach(m -> {
            try {
                Dynamic dynamic = objectMapper.readValue(m.getValue(), Dynamic.class);
                // 设置最新的音乐点赞数量（score）
                dynamicMapper.updateLikeCount(dynamic.getId(), m.getScore().intValue());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
