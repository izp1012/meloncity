package com.meloncity.citiz.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

/**
 * WebSocket 설정을 담당하는 Configuration 클래스
 * STOMP 프로토콜을 사용한 WebSocket 메시지 브로커 설정
 */
@Configuration
@EnableWebSocketMessageBroker
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${websocket.endpoint}")
    private String endpoint;

    @Value("#{'${websocket.message-broker}'.split(',')}")
    private List<String> messageBroker;

    @Value("${websocket.destination-prefix}")
    private String destinationPrefix;

    /**
     * 클라이언트가 WebSocket에 연결할 수 있는 엔드포인트 등록
     * SockJS를 사용하여 브라우저 호환성 지원
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(endpoint)
                .setAllowedOriginPatterns("*")  // CORS 설정 (프로덕션에서는 구체적인 도메인 지정 권장)
                .withSockJS();  // SockJS fallback 옵션 활성화

        log.info("STOMP endpoint registered: {}", endpoint);
    }

    /**
     * 메시지 브로커 설정
     * - /topic: 다대다 통신 (채팅방)
     * - /queue: 일대일 통신 (개인 메시지)
     * - /app: 클라이언트에서 서버로 보내는 메시지의 prefix
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 메시지 브로커가 처리할 prefix 설정
        config.enableSimpleBroker(messageBroker.toArray(new String[0]));

        // 클라이언트에서 서버로 보내는 메시지의 prefix 설정
        config.setApplicationDestinationPrefixes(destinationPrefix);

        // 사용자별 개인 메시지를 위한 prefix 설정
        config.setUserDestinationPrefix("/user");

        log.info("Message broker configured - broker: {}, destination prefix: {}",
                messageBroker, destinationPrefix);
    }
}