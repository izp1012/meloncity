package com.meloncity.citiz.service;

import com.meloncity.citiz.dto.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaChatConsumer {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    /**
     * Kafka Topic으로부터 메시지를 수신하여 WebSocket으로 브로드캐스트합니다.
     */
    @KafkaListener(
            topics = "${kafka.topic.chat:chat-messages}",
            groupId = "${spring.kafka.consumer.group-id:chat-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ChatMessageDto messageDto) {
        log.info("Kafka 메시지 수신 - Room: {}, Sender: {}, Content: {}",
                messageDto.getRoomId(), messageDto.getSenderId(), messageDto.getContent());

        try {
            // 1. 데이터베이스에 메시지 저장 (RedisStreamService의 기존 로직과 동일하게 유지 가능)
            // 기존에는 Redis Stream ID를 넘겼으나, 여기서는 Kafka Offset 등을 넘길 수 있음.
            // 일단은 식별자 용도로 "KAFKA_" 접두어를 붙인 tempId 등을 활용
            Long messageId = chatService.saveMessageToDatabase(messageDto, "KAFKA_" + System.currentTimeMillis());
            messageDto.setMessageId(messageId);

            // 2. WebSocket으로 브로드캐스트
            String destination = "/topic/chat/room/" + messageDto.getRoomId();
            messagingTemplate.convertAndSend(destination, messageDto);
            
            log.info("WebSocket 브로드캐스트 완료 - Destination: {}", destination);
            
        } catch (Exception e) {
            log.error("메시지 처리 중 오류 발생", e);
        }
    }
}
