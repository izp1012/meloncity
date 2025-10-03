package com.meloncity.citiz.config.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.connection.stream.StreamInfo;

import jakarta.annotation.PostConstruct;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Map;

/**
 * Redis Stream 초기화를 담당하는 클래스
 * 애플리케이션 시작 시 필요한 Stream과 Consumer Group을 생성
 */
@Slf4j
public class RedisStreamInitializer {

    private final RedisTemplate<String, Object> redisTemplate;
    private final String streamName;
    private final String consumerGroup;

    public RedisStreamInitializer(RedisTemplate<String, Object> redisTemplate, String streamName, String consumerGroup) {
        Assert.notNull(redisTemplate, "RedisTemplate must not be null");
        Assert.hasText(streamName, "StreamName must not be empty");
        Assert.hasText(consumerGroup, "ConsumerGroup must not be empty");

        this.redisTemplate = redisTemplate;
        this.streamName = streamName;
        this.consumerGroup = consumerGroup;
    }

    /**
     * 애플리케이션 시작 시 Redis Stream과 Consumer Group 초기화
     * Stream이 존재하지 않으면 생성하고, Consumer Group도 생성
     */
    @PostConstruct
    public void initializeStream() {
        try {
            log.info("Redis Stream 초기화 시작 - Stream: {}, Consumer Group: {}", streamName, consumerGroup);

            // Stream이 존재하는지 확인 및 생성
            ensureStreamExists();

            // Consumer Group이 존재하는지 확인하고 생성
            ensureConsumerGroupExists();
        } catch (RuntimeException e) {
            log.error("Redis Stream 초기화 실패 - Stream: {}, Consumer Group: {}",
                    streamName, consumerGroup, e);
            throw e;
        }

        log.info("Redis Stream 초기화 완료 - Stream: {}, Consumer Group: {}", streamName, consumerGroup);
    }

    /**
     * Redis Stream이 존재하도록 보장합니다.
     * Stream이 존재하지 않으면, RedisTemplate의 add()를 통해 자동으로 생성됩니다.
     */
    private void ensureStreamExists() {
        try {
            // Stream 존재 여부를 확인합니다.
            redisTemplate.opsForStream().info(streamName);
            log.debug("Redis Stream '{}'이 이미 존재합니다.", streamName);
        } catch (DataAccessException e) {
            // 스트림이 없으면 예외(Stream key not found)가 발생하므로,
            // 더미 메시지를 추가해 스트림을 생성합니다.
            log.info("Redis Stream '{}'이 존재하지 않아 생성합니다.", streamName);
            try {
                // Redis의 XADD 명령어는 key가 존재하지 않으면 자동으로 생성합니다.
                Map<String, Object> dummyMessage = Collections.singletonMap("initializer", "stream-created");
                redisTemplate.opsForStream().add(streamName, dummyMessage);
                log.info("Redis Stream '{}' 생성 완료.", streamName);
            } catch (Exception ex) {
                // 동시성 문제로 인해 다른 인스턴스가 먼저 생성했을 수 있습니다.
                // 이 경우 재확인하여 예외를 무시할지 결정합니다.
                try {
                    redisTemplate.opsForStream().info(streamName);
                    log.debug("Redis Stream '{}'이 동시성 문제로 인해 이미 생성되었습니다.", streamName);
                } catch (Exception recheckEx) {
                    log.error("Redis Stream '{}' 생성 중 복구 불가능한 오류 발생.", streamName, recheckEx);
                    throw new RuntimeException("Stream 생성 실패: " + streamName, recheckEx);
                }
            }
        }
    }

    /**
     * Consumer Group이 존재하도록 보장합니다.
     * 그룹이 존재하지 않으면 생성합니다.
     */
    private void ensureConsumerGroupExists() {
        try {
            // Consumer Group이 이미 존재하는지 확인합니다.
            redisTemplate.opsForStream().createGroup(streamName, ReadOffset.from("0"), consumerGroup);
            log.info("Consumer Group '{}' 생성 완료.", consumerGroup);
        } catch (DataAccessException e) {
            // BUSYGROUP 에러는 그룹이 이미 존재한다는 의미
            if (e.getMessage() != null && e.getCause().toString().contains("BUSYGROUP")) {
                log.debug("Consumer Group '{}'이 이미 존재합니다.", consumerGroup);
            } else {
                // 다른 Redis 관련 오류는 재시도 또는 오류 보고가 필요합니다.
                log.error("Consumer Group '{}' 생성 중 Redis 오류 발생.", consumerGroup, e);
                throw new RuntimeException("Consumer Group 생성 실패: " + consumerGroup, e);
            }
        }
    }

    /**
     * Stream과 Consumer Group의 상태 정보를 로깅하는 메서드
     * 디버깅 및 모니터링 목적
     */
    public void logStreamStatus() {
        try {
            StreamInfo.XInfoStream streamInfo = redisTemplate.opsForStream().info(streamName);
            log.info("▶️ Redis Stream 정보");
            log.info("  - Stream Name: {}", streamName);
            log.info("  - Stream Length: {}", streamInfo.streamLength());
            log.info("  - Group Count: {}", streamInfo.groupCount());

            StreamInfo.XInfoGroups groups = redisTemplate.opsForStream().groups(streamName);
            log.info("▶️ Consumer Group 정보");
            groups.forEach(group ->
                    log.info("  - Group Name: {}, Consumers: {}, Pending: {}",
                            group.groupName(), group.consumerCount(), group.pendingCount())
            );
        } catch (DataAccessException e) {
            // Stream이 존재하지 않을 때 발생하는 예외 처리
            if (e.getMessage() != null && e.getMessage().contains("ERR no such key")) {
                log.warn("🚨 Redis Stream '{}'이 존재하지 않아 상태를 조회할 수 없습니다.", streamName);
            } else {
                // 그 외의 Redis 관련 오류
                log.error("❌ Redis Stream '{}' 상태 조회 중 오류 발생.", streamName, e);
            }
        } catch (Exception e) {
            log.error("❌ Redis Stream '{}' 상태 조회 중 예상치 못한 오류 발생.", streamName, e);
        }
    }

    /**
     * Redis Stream과 Consumer Group을 완전히 삭제하는 메서드
     * 개발/테스트 환경에서만 사용 권장
     */
    public void cleanupStream() {
        try {
            // 단일 키(String)를 Collections.singleton()을 사용해 Collection으로 변환
            Long deletedCount = redisTemplate.delete(Collections.singleton(streamName));
            if (deletedCount > 0) {
                log.info("✅ Redis Stream '{}' 및 관련 Consumer Group이 성공적으로 삭제되었습니다.", streamName);
            } else {
                log.warn("⚠️ Redis Stream '{}'이 이미 존재하지 않아 삭제할 수 없습니다.", streamName);
            }
        } catch (Exception e) {
            log.error("❌ Redis Stream '{}' 삭제 중 오류 발생.", streamName, e);
        }
    }

}