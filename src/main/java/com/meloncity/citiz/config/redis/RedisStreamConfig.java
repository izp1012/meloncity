package com.meloncity.citiz.config.redis;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Redis Stream 설정을 담당하는 Configuration 클래스
 * 채팅 메시지 스트림 처리를 위한 설정을 제공
 */
@Configuration
@Slf4j
public class RedisStreamConfig {

    @Value("${redis.chat.stream.name:chat-stream}")
    private String streamName;

    @Value("${redis.chat.stream.consumer-group:chat-group}")
    private String consumerGroup;

    @Value("${redis.chat.stream.consumer-name:chat-consumer-${random.uuid}}")
    private String consumerName;

    @Value("${redis.chat.stream.block-timeout:2000}")
    private long blockTimeout;

    @Value("${redis.chat.stream.batch-size:10}")
    private int batchSize;

    /**
     * Redis Stream 초기화를 담당하는 Bean
     * 애플리케이션 시작 시 필요한 Stream과 Consumer Group을 생성
     */
    //@Bean
    public RedisStreamInitializer redisStreamInitializer(RedisTemplate<String, Object> redisTemplate) {
        log.info("Creating Redis Stream Initializer for stream: {}, consumer group: {}", 
                streamName, consumerGroup);
        
        return new RedisStreamInitializer(redisTemplate, streamName, consumerGroup);
    }

    /**
     * Redis Stream 설정 정보를 담는 Bean
     * 다른 서비스에서 스트림 설정을 쉽게 사용할 수 있도록 제공
     */
    //@Bean
//    public StreamSettings streamSettings() {
//        return StreamSettings.builder()
//                .streamName(streamName)
//                .consumerGroup(consumerGroup)
//                .consumerName(consumerName)
//                .blockTimeout(blockTimeout)
//                .batchSize(batchSize)
//                .build();
//    }

    /**
     * Redis Stream 설정 정보를 담는 클래스
     */
//    @Getter
//    @Builder
//    public static class StreamSettings {
//        private final String streamName;
//        private final String consumerGroup;
//        private final String consumerName;
//        private final long blockTimeout;
//        private final int batchSize;
//    }
}