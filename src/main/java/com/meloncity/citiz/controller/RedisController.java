package com.meloncity.citiz.controller;

import com.meloncity.citiz.dto.PublishRequest;
import com.meloncity.citiz.dto.StreamRequest;
import com.meloncity.citiz.dto.TestChatRequest;
import com.meloncity.citiz.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis Pub/Sub 및 Stream 기능을 직접 호출할 수 있는 컨트롤러
 * 테스트 및 관리 목적으로 사용
 */
@RestController
@RequestMapping("/api/redis")
@RequiredArgsConstructor
@Slf4j
public class RedisController {

    private final RedisService redisService;

    /**
     * Redis Pub/Sub을 통해 메시지를 발행합니다.
     * POST /api/redis/publish
     * 
     * @param request 발행할 메시지 정보
     * @return 발행 결과
     */
    @PostMapping("/publish")
    public ResponseEntity<String> publishMessage(@RequestBody PublishRequest request) {
        
        log.info("Redis Pub/Sub 메시지 발행 요청 - Message: {}", request.getMessage());
        
        try {
            redisService.publish(request.getMessage());
            log.info("Redis Pub/Sub 메시지 발행 완료 - Message: {}", request.getMessage());
            return ResponseEntity.ok("메시지가 성공적으로 발행되었습니다.");
        } catch (Exception e) {
            log.error("Redis Pub/Sub 메시지 발행 중 오류 발생", e);
            return ResponseEntity.internalServerError().body("메시지 발행 중 오류가 발생했습니다.");
        }
    }

    /**
     * Redis Stream에 메시지를 추가합니다.
     * POST /api/redis/stream
     * 
     * @param request 스트림에 추가할 메시지 정보
     * @return 추가 결과
     */
    @PostMapping("/stream")
    public ResponseEntity<String> addToStream(@RequestBody StreamRequest request) {
        
        log.info("Redis Stream 메시지 추가 요청 - Stream: {}, Content: {}", 
                request.getStreamKey(), request.getContent());
        
        try {
            // 메시지 맵 생성
            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("content", request.getContent());
            messageMap.put("sender", request.getSender());
            messageMap.put("timestamp", LocalDateTime.now().toString());
            
            // 추가 데이터가 있으면 포함
            if (request.getAdditionalData() != null) {
                messageMap.putAll(request.getAdditionalData());
            }
            
            redisService.addToStream(request.getStreamKey(), messageMap);
            
            log.info("Redis Stream 메시지 추가 완료 - Stream: {}", request.getStreamKey());
            return ResponseEntity.ok("메시지가 Stream에 성공적으로 추가되었습니다.");
        } catch (Exception e) {
            log.error("Redis Stream 메시지 추가 중 오류 발생", e);
            return ResponseEntity.internalServerError().body("Stream 메시지 추가 중 오류가 발생했습니다.");
        }
    }

    /**
     * 테스트용 채팅 메시지를 Stream에 추가합니다.
     * POST /api/redis/test-chat
     * 
     * @param request 테스트 채팅 메시지 정보
     * @return 추가 결과
     */
    @PostMapping("/test-chat")
    public ResponseEntity<String> addTestChatMessage(@RequestBody TestChatRequest request) {
        
        log.info("테스트 채팅 메시지 Stream 추가 - Room: {}, Sender: {}, Content: {}", 
                request.getRoomId(), request.getSenderId(), request.getContent());
        
        try {
            // 채팅 메시지 형태의 맵 생성
            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("roomId", request.getRoomId());
            messageMap.put("senderId", request.getSenderId());
            messageMap.put("senderName", request.getSenderName());
            messageMap.put("content", request.getContent());
            messageMap.put("type", "CHAT");
            messageMap.put("status", "SENT");
            messageMap.put("timestamp", LocalDateTime.now().toString());
            
            redisService.addToStream("chat-stream", messageMap);
            
            log.info("테스트 채팅 메시지 Stream 추가 완료 - Room: {}", request.getRoomId());
            return ResponseEntity.ok("테스트 채팅 메시지가 성공적으로 추가되었습니다.");
        } catch (Exception e) {
            log.error("테스트 채팅 메시지 추가 중 오류 발생", e);
            return ResponseEntity.internalServerError().body("테스트 메시지 추가 중 오류가 발생했습니다.");
        }
    }

    /**
     * Redis 연결 상태를 확인합니다.
     * GET /api/redis/health
     * 
     * @return 연결 상태
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> checkRedisHealth() {
        
        log.info("Redis 연결 상태 확인 요청");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Redis 연결 테스트를 위해 간단한 операция 수행
            redisService.publish("health-check");
            
            response.put("status", "UP");
            response.put("message", "Redis 연결이 정상입니다.");
            response.put("timestamp", LocalDateTime.now());
            
            log.info("Redis 연결 상태 확인 완료 - 정상");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("message", "Redis 연결에 문제가 있습니다.");
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            
            log.error("Redis 연결 상태 확인 실패", e);
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Redis Stream 정보를 조회합니다.
     * GET /api/redis/stream/{streamKey}/info
     * 
     * @param streamKey 조회할 스트림 키
     * @return 스트림 정보
     */
    @GetMapping("/stream/{streamKey}/info")
    public ResponseEntity<Map<String, Object>> getStreamInfo(@PathVariable String streamKey) {
        
        log.info("Redis Stream 정보 조회 요청 - Stream: {}", streamKey);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // TODO: RedisService에 스트림 정보 조회 메서드 추가 필요
            response.put("streamKey", streamKey);
            response.put("message", "스트림 정보 조회 기능은 RedisService에서 구현 필요");
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Redis Stream 정보 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "스트림 정보 조회 중 오류가 발생했습니다.",
                "message", e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }
}