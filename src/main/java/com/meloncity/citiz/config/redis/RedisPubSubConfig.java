package com.meloncity.citiz.config.redis;

import com.meloncity.citiz.service.ChatMessageSubscriber;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * Redis Pub/Sub 설정을 담당하는 Configuration 클래스
 * 채팅 메시지 발행/구독 기능을 위한 설정을 제공
 */
@Configuration
@Slf4j
public class RedisPubSubConfig {

    @Value("${redis.chat.pubsub.channel.chatroom:chatroom}")
    private String chatroomChannel;

    @Value("${redis.chat.pubsub.channel.join:chat:join}")
    private String joinChannel;

    @Value("${redis.chat.pubsub.channel.leave:chat:leave}")
    private String leaveChannel;

    @Value("${redis.chat.pubsub.channel.notification:chat:notification}")
    private String notificationChannel;

    /**
     * 채팅방 메시지를 위한 Pub/Sub 채널 설정
     */
    @Bean("chatroomTopic")
    public ChannelTopic chatroomTopic() {
        log.info("Creating chatroom topic: {}", chatroomChannel);
        return new ChannelTopic(chatroomChannel);
    }

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
     * 채팅 메시지 구독자를 위한 MessageListenerAdapter 생성
     */
    @Bean
    public MessageListenerAdapter chatMessageListenerAdapter(ChatMessageSubscriber chatMessageSubscriber) {
        log.info("Creating chat message listener adapter");
        return new MessageListenerAdapter(chatMessageSubscriber, "onMessage");
    }

    /**
     * Redis Message Listener Container 생성
     * Pub/Sub 메시지를 수신하기 위한 컨테이너
     * 여러 채널을 동시에 구독 가능
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListener(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter chatMessageListenerAdapter,
            ChannelTopic chatroomTopic,
            ChannelTopic joinTopic,
            ChannelTopic leaveTopic,
            ChannelTopic notificationTopic) {
        
        log.info("Configuring Redis Message Listener Container");
        
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        
        // 여러 채널에 동일한 리스너 등록
        container.addMessageListener(chatMessageListenerAdapter, chatroomTopic);
        container.addMessageListener(chatMessageListenerAdapter, joinTopic);
        container.addMessageListener(chatMessageListenerAdapter, leaveTopic);
        container.addMessageListener(chatMessageListenerAdapter, notificationTopic);
        
        // 컨테이너 설정
        container.setTaskExecutor(null); // 기본 TaskExecutor 사용
        container.setSubscriptionExecutor(null); // 기본 SubscriptionExecutor 사용
        
        log.info("Redis Message Listener Container configured with channels: {}, {}, {}, {}", 
                chatroomChannel, joinChannel, leaveChannel, notificationChannel);
        
        return container;
    }

    /**
     * Pub/Sub 설정 정보를 반환하는 Bean
     * 다른 서비스에서 채널 정보를 쉽게 사용할 수 있도록 제공
     */
    @Bean
    public PubSubChannels pubSubChannels() {
        return PubSubChannels.builder()
                .chatroomChannel(chatroomChannel)
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
        private final String chatroomChannel;
        private final String joinChannel;
        private final String leaveChannel;
        private final String notificationChannel;
    }
}