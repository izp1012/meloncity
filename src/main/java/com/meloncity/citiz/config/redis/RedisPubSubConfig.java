package com.meloncity.citiz.config.redis;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * Redis Pub/Sub 설정을 담당하는 Configuration 클래스
 * 채팅 메시지 발행/구독 기능을 위한 설정을 제공
 */
@Configuration
@Slf4j
public class RedisPubSubConfig {

    @Value("${redis.chat.pubsub.channel.join:chat:join}")
    private String joinChannel;

    @Value("${redis.chat.pubsub.channel.leave:chat:leave}")
    private String leaveChannel;

    @Value("${redis.chat.pubsub.channel.notification:chat:notification}")
    private String notificationChannel;

    /**
     * 입장 알림을 위한 Pub/Sub 채널 설정
     */
    @Bean("joinTopic")
    public ChannelTopic joinTopic() {
        log.info("Creating join topic: {}", joinChannel);
        return new ChannelTopic(joinChannel);
    }

    /**
     * 퇴장 알림을 위한 Pub/Sub 채널 설정
     */
    @Bean("leaveTopic")
    public ChannelTopic leaveTopic() {
        log.info("Creating leave topic: {}", leaveChannel);
        return new ChannelTopic(leaveChannel);
    }

    /**
     * 일반 알림을 위한 Pub/Sub 채널 설정
     */
    @Bean("notificationTopic")
    public ChannelTopic notificationTopic() {
        log.info("Creating notification topic: {}", notificationChannel);
        return new ChannelTopic(notificationChannel);
    }

    /**
     * Redis Message Listener Container 생성
     * Pub/Sub 메시지를 수신하기 위한 컨테이너
     * 여러 채널을 동시에 구독 가능
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListener(RedisConnectionFactory connectionFactory) {

        log.info("Configuring Redis Message Listener Container for non-persistent notifications.");

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        log.info("Redis Message Listener Container configured with channels: {}, {}, {}",
                joinChannel, leaveChannel, notificationChannel);
        
        return container;
    }

    /**
     * Pub/Sub 설정 정보를 반환하는 Bean
     * 다른 서비스에서 채널 정보를 쉽게 사용할 수 있도록 제공
     */
    @Bean
    public PubSubChannels pubSubChannels() {
        return PubSubChannels.builder()
                .joinChannel(joinChannel)
                .leaveChannel(leaveChannel)
                .notificationChannel(notificationChannel)
                .build();
    }

    /**
     * Pub/Sub 채널 정보를 담는 설정 클래스
     */
    @Getter
    @Builder
    public static class PubSubChannels {
        private final String joinChannel;
        private final String leaveChannel;
        private final String notificationChannel;
    }
}