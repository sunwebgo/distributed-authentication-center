package com.mc.dynamic.listener;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mc.common.constants.CacheConstants;
import com.mc.common.constants.MQConstants;
import com.mc.common.entity.vo.comment.CommentVO;
import com.mc.common.utils.RedisUtil;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class CommentDataChangeListener {

    /**
     * 监听新增评论数据（用户用户消息）
     *
     * @param comment
     * @param message
     * @param channel
     */
    @RabbitListener(queues = MQConstants.COMMENT_DATA_QUEUE)
    public void dynamicDataInsert(CommentVO comment, Message message, Channel channel) {
        ObjectMapper objectMapper = new ObjectMapper();
        String commentStr;
        try {
            commentStr = objectMapper.writeValueAsString(comment);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        // 缓存存储评论信息，key为被评论者id，field为评论id，value为是评论信息
        RedisUtil.hashPut(
                CacheConstants.COMMENT_RESP + comment.getByCommentUId(),
                comment.getId().toString(),
                commentStr);
        // 消费端手动ACK
        try {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
