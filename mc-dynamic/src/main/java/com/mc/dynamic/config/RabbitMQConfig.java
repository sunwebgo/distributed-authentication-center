package com.mc.dynamic.config;

import com.mc.common.constants.MQConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    /**
     * @return {@link Exchange}
     */
    @Bean
    public Exchange commentSyncExchange() {
        return new TopicExchange(
                MQConstants.COMMENT_SYNC_EXCHANGE,
                true,
                false);
    }

    @Bean
    public Queue commentSyncQueue() {
        return new Queue(
                MQConstants.COMMENT_DATA_QUEUE,
                true,
                false,
                false);
    }

    @Bean
    public Binding dynamicDataSyncBinding() {
        return new Binding(
                MQConstants.COMMENT_DATA_QUEUE,
                Binding.DestinationType.QUEUE,
                MQConstants.COMMENT_SYNC_EXCHANGE,
                MQConstants.COMMENT_DATA_KEY,
                null);
    }
}
