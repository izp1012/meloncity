//package com.meloncity.citiz.service;
//
//import com.meloncity.citiz.config.redis.RedisStreamConfig.StreamSettings;
//import com.meloncity.citiz.domain.chat.ChatStatus;
//import com.meloncity.citiz.domain.chat.MessageType;
//import com.meloncity.citiz.dto.ChatMessageDto;
//import lombok.Getter;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.DisposableBean;
//import org.springframework.data.redis.connection.stream.*;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//
//import java.time.Duration;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Map;
//
///**
// * Redis Stream을 이용한 메시지 처리 서비스
// * Stream에서 메시지를 소비하고 WebSocket으로 전달하는 역할
// */
////@Service
//@RequiredArgsConstructor
//@Slf4j
//public class RedisStreamService implements DisposableBean {
//
//    private final RedisTemplate<String, Object> redisTemplate;
//    private final SimpMessagingTemplate messagingTemplate;
//    private final ChatService chatService;
//    private Thread consumerThread; // 현재 스레드를 저장할 필드
//
////    @Getter
////    private final StreamSettings streamSettings; // 주입된 StreamSettings 사용
//
//    /**
//     * Redis Stream에서 메시지를 지속적으로 소비합니다.
//     * 새로운 메시지가 도착하면 WebSocket을 통해 클라이언트에게 전달
//     */
//    @Async("taskExecutor") // 별도의 스레드 풀 사용
//    public void startConsumingMessages() {
//        this.consumerThread = Thread.currentThread();
//
//        log.info("Redis Stream 메시지 소비 시작 - Stream: {}, Group: {}, Consumer: {}",
//                streamSettings.getStreamName(),
//                streamSettings.getConsumerGroup(),
//                streamSettings.getConsumerName());
//
////        while (!Thread.currentThread().isInterrupted()) {
//        while (this.consumerThread != null && !this.consumerThread.isInterrupted()) {
//
//                try {
//                // Consumer Group을 통해 새로운 메시지 읽기 - 설정된 값들 사용
//                List<MapRecord<String, Object, Object>> records = redisTemplate.opsForStream()
//                        .read(Consumer.from(streamSettings.getConsumerGroup(), streamSettings.getConsumerName()),
//                                StreamReadOptions.empty()
//                                        .count(streamSettings.getBatchSize())
//                                        .block(Duration.ofMillis(streamSettings.getBlockTimeout())),
//                                StreamOffset.create(streamSettings.getStreamName(), ReadOffset.lastConsumed()));
//
//                if (records != null && !records.isEmpty()) {
//                    processRecords(records);
//                }
//
//            } catch (Exception e) {
//                if (e instanceof IllegalStateException && e.getMessage() != null && e.getMessage().contains("STOPPED")) {
//                    log.warn("Redis 연결 팩토리가 중지되어 Stream 소비를 중단합니다.");
//                    consumerThread.interrupt(); // 팩토리가 중지되면 루프 종료
//                    break;
//                }
//
//                log.error("Redis Stream 메시지 소비 중 오류 발생", e);
//                // 잠시 대기 후 재시도
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException ie) {
//                    Thread.currentThread().interrupt();
//                    break;
//                }
//            }
//        }
//    }
//
//    /**
//     * 여러 레코드를 일괄 처리합니다.
//     * @param records 처리할 레코드 목록
//     */
//    private void processRecords(List<MapRecord<String, Object, Object>> records) {
//        for (MapRecord<String, Object, Object> record : records) {
//            try {
//                processMessage(record);
//
//                // 메시지 처리 완료 후 ACK
//                redisTemplate.opsForStream().acknowledge(
//                        streamSettings.getStreamName(),
//                        streamSettings.getConsumerGroup(),
//                        record.getId());
//
//                log.debug("메시지 처리 및 ACK 완료 - ID: {}", record.getId().getValue());
//
//            } catch (Exception e) {
//                log.error("레코드 처리 중 오류 발생 - ID: {}", record.getId().getValue(), e);
//                // 개별 메시지 처리 실패는 전체 배치를 중단시키지 않음
//            }
//        }
//    }
//
//    /**
//     * Redis Stream에서 읽은 메시지를 처리합니다.
//     * @param record Redis Stream 레코드
//     */
//    private void processMessage(MapRecord<String, Object, Object> record) {
//        String streamId = record.getId().getValue();
//        Map<Object, Object> messageMap = record.getValue();
//
//        try {
//            log.debug("Stream 메시지 처리 시작 - Stream ID: {}, Data: {}", streamId, messageMap);
//
//            // Stream 데이터를 DTO로 변환
//            ChatMessageDto messageDto = convertToMessageDto(messageMap, streamId);
//
//            log.info("Stream 메시지 처리 중 - Stream ID: {}, Room: {}, Sender: {}",
//                    streamId, messageDto.getRoomId(), messageDto.getSenderId());
//
//            // 데이터베이스에 메시지 저장 (선택적)
//            if (shouldSaveToDatabase(messageDto)) {
//                chatService.saveMessageToDatabase(messageDto, streamId);
//            }
//
//            // WebSocket을 통해 클라이언트에게 메시지 전달
//            broadcastMessage(messageDto);
//
//            // 메시지 상태를 DELIVERED로 업데이트
//            messageDto.setStatus(ChatStatus.DELIVERED);
//
//        } catch (Exception e) {
//            log.error("메시지 처리 중 오류 발생 - Stream ID: {}", streamId, e);
//            throw e; // 상위에서 ACK 처리를 결정할 수 있도록 예외 재발생
//        }
//    }
//
//    /**
//     * 메시지를 데이터베이스에 저장할지 결정합니다.
//     * @param messageDto 메시지 DTO
//     * @return 저장 여부
//     */
//    private boolean shouldSaveToDatabase(ChatMessageDto messageDto) {
//        // 시스템 메시지나 테스트 메시지는 저장하지 않음
//        return messageDto.getType() == MessageType.CHAT &&
//                messageDto.getSenderId() != null &&
//                messageDto.getSenderId() > 0;
//    }
//
//    /**
//     * WebSocket을 통해 채팅방의 모든 참여자에게 메시지를 브로드캐스트합니다.
//     * @param messageDto 브로드캐스트할 메시지
//     */
//    private void broadcastMessage(ChatMessageDto messageDto) {
//        try {
//            String destination = "/topic/chat/" + messageDto.getRoomId();
//            messagingTemplate.convertAndSend(destination, messageDto);
//
//            log.debug("메시지 브로드캐스트 완료 - Destination: {}, Content: {}",
//                    destination, messageDto.getContent());
//        } catch (Exception e) {
//            log.error("메시지 브로드캐스트 중 오류 발생", e);
//            throw e;
//        }
//    }
//
//    /**
//     * Redis Stream 맵 데이터를 ChatMessageDto로 변환합니다.
//     * @param messageMap Redis Stream에서 읽은 메시지 맵
//     * @param streamId Stream ID
//     * @return 변환된 ChatMessageDto
//     */
//    private ChatMessageDto convertToMessageDto(Map<Object, Object> messageMap, String streamId) {
//        try {
//            ChatMessageDto.ChatMessageDtoBuilder builder = ChatMessageDto.builder()
//                    .roomId(Long.valueOf(messageMap.get("roomId").toString()))
//                    .senderId(Long.valueOf(messageMap.get("senderId").toString()))
//                    .senderName(messageMap.get("senderName").toString())
//                    .content(messageMap.get("content").toString())
//                    .type(MessageType.valueOf(messageMap.get("type").toString()))
//                    .status(ChatStatus.valueOf(messageMap.get("status").toString()))
//                    .timestamp(LocalDateTime.parse(messageMap.get("timestamp").toString()))
//                    .streamId(streamId);
//
//            // tempId가 있는 경우에만 설정
//            if (messageMap.containsKey("tempId") && messageMap.get("tempId") != null) {
//                builder.tempId(messageMap.get("tempId").toString());
//            }
//
//            return builder.build();
//
//        } catch (Exception e) {
//            log.error("메시지 DTO 변환 중 오류 발생 - StreamId: {}, Data: {}", streamId, messageMap, e);
//            throw new RuntimeException("메시지 DTO 변환 실패", e);
//        }
//    }
//
//    /**
//     * 처리되지 않은 메시지들을 조회하고 처리합니다.
//     * 애플리케이션 재시작 시 누락된 메시지 처리를 위한 메서드
//     */
//    public void processPendingMessages() {
//        try {
//            // Pending 메시지 조회
//            PendingMessagesSummary pendingSummary = redisTemplate.opsForStream()
//                    .pending(streamSettings.getStreamName(), streamSettings.getConsumerGroup());
//
//            assert pendingSummary != null;
//            long totalPending = pendingSummary.getTotalPendingMessages();
//            if (totalPending <= 0) {
//                log.info("처리되지 않은 Pending 메시지가 없습니다.");
//                return;
//            }
//
//            log.warn("처리되지 않은 메시지 {}개 발견. Pending 메시지 재처리를 시작합니다.", totalPending);
//
//            List<MapRecord<String, Object, Object>> pendingRecords;
//            ReadOffset offset = ReadOffset.from("0"); // 가장 오래된 Pending 메시지부터 시작
//
//            do {
//                pendingRecords = redisTemplate.opsForStream()
//                        .read(Consumer.from(streamSettings.getConsumerGroup(), streamSettings.getConsumerName()),
//                                StreamReadOptions.empty().count(streamSettings.getBatchSize()),
//                                StreamOffset.create(streamSettings.getStreamName(), offset));
//
//                if (pendingRecords != null && !pendingRecords.isEmpty()) {
//                    processRecords(pendingRecords);
//                    // 마지막으로 처리된 레코드의 다음 ID를 새로운 Offset으로 설정하여 다음 배치 처리
//                    offset = ReadOffset.from(pendingRecords.get(pendingRecords.size() - 1).getId().getValue());
//                }
//
//            } while (pendingRecords != null && !pendingRecords.isEmpty());
//
//            log.info("Pending 메시지 재처리 완료.");
//
//        } catch (Exception e) {
//            log.error("Pending 메시지 처리 중 오류 발생", e);
//        }
//    }
//
//    /**
//     * Consumer Group 정보를 조회합니다.
//     * 모니터링 및 디버깅 목적
//     */
//    public void logConsumerGroupInfo() {
//        try {
//            StreamInfo.XInfoGroups groups = redisTemplate.opsForStream()
//                    .groups(streamSettings.getStreamName());
//
//            for (StreamInfo.XInfoGroup group : groups) {
//                log.info("Consumer Group Info - Name: {}, Consumers: {}, Pending: {}, Last Delivered: {}",
//                        group.groupName(), group.consumerCount(),
//                        group.pendingCount(), group.lastDeliveredId());
//            }
//        } catch (Exception e) {
//            log.error("Consumer Group 정보 조회 중 오류 발생", e);
//        }
//    }
//
//    /**
//     * 스트림 소비를 중지합니다.
//     */
//    public void stopConsuming() {
//        log.info("Redis Stream 소비 중지 요청됨");
//        // 현재 스레드 인터럽트를 통해 소비 루프를 중지
//        // 실제로는 더 정교한 종료 메커니즘이 필요할 수 있음
//    }
//
//    /**
//     * Spring 컨테이너 종료 시 호출되어, 비동기 스레드를 안전하게 종료합니다.
//     */
//    @Override
//    public void destroy() {
//        log.info("Consumer 스레드 인터럽트 요청 중...");
//        if (this.consumerThread != null) {
//            this.consumerThread.interrupt(); // 소비 스레드에 인터럽트 요청
//
//             //필요하다면, 스레드가 종료될 때까지 잠시 대기할 수 있습니다. (예: 3초)
//             try {
//                 this.consumerThread.join(3000);
//             } catch (InterruptedException e) {
//                 Thread.currentThread().interrupt();
//             }
//        }
//        this.stopConsuming(); // 기존 stopConsuming 로직이 있다면 추가 실행
//        log.info("Consumer 스레드 인터럽트 요청 완료.");
//    }
//
//}