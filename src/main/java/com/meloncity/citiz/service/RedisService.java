package com.meloncity.citiz.service;

import com.meloncity.citiz.config.redis.RedisPubSubConfig.*;
import com.meloncity.citiz.config.redis.RedisStreamConfig.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamInfo;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * RedisService: Redis Pub/Sub 및 Streams를 활용한 서비스
 * 기본적인 Redis 기능들과 채팅 시스템을 위한 확장 기능 제공
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    @Getter
    private final PubSubChannels pubSubChannels;
    @Getter
    private final StreamSettings streamSettings;

    // ==================== Pub/Sub 관련 메서드 ====================

    /**
     * Redis Pub/Sub를 이용해 메시지를 발행하는 메서드
     * @param message 발행할 메시지
     */
    public void publish(String message) {
        try {
            // 기본 채널에 메시지 발행 (테스트용)
            redisTemplate.convertAndSend("test-channel", message);
            log.debug("메시지가 test-channel에 발행됨: {}", message);
        } catch (Exception e) {
            log.error("메시지 발행 중 오류 발생: {}", message, e);
            throw e;
        }
    }

    /**
     * 특정 채널에 메시지를 발행하는 메서드
     * @param channel 채널명
     * @param message 발행할 메시지
     */
    public void publishToChannel(String channel, String message) {
        try {
            redisTemplate.convertAndSend(channel, message);
            log.debug("메시지가 {}에 발행됨: {}", channel, message);
        } catch (Exception e) {
            log.error("채널 {} 메시지 발행 중 오류 발생: {}", channel, message, e);
            throw e;
        }
    }

    // ==================== Stream 관련 메서드 ====================

    /**
     * Redis Stream에 메시지를 저장하는 메서드 (기본 스트림 사용)
     * @param message 저장할 메시지 (Map 형태)
     * @return Stream에 추가된 메시지의 ID
     */
    public String addToStream(Map<String, Object> message) {
        return addToStream(streamSettings.getStreamName(), message);
    }

    /**
     * Redis Stream에 메시지를 저장하는 메서드
     * @param streamKey 스트림 키
     * @param message 저장할 메시지 (Map 형태)
     * @return Stream에 추가된 메시지의 ID
     */
    public String addToStream(String streamKey, Map<String, Object> message) {
        try {
            RecordId recordId = redisTemplate.opsForStream().add(streamKey, message);
            log.debug("메시지가 스트림 {}에 추가됨 - ID: {}", streamKey, recordId.getValue());
            return recordId.getValue();
        } catch (Exception e) {
            log.error("스트림 {} 메시지 추가 중 오류 발생", streamKey, e);
            throw e;
        }
    }

    /**
     * Redis Stream 정보를 조회하는 메서드
     * @param streamKey 조회할 스트림 키
     * @return 스트림 정보 맵
     */
    public Map<String, Object> getStreamInfo(String streamKey) {
        Map<String, Object> info = new HashMap<>();
        
        try {
            StreamInfo.XInfoStream streamInfo = redisTemplate.opsForStream().info(streamKey);
            
            info.put("streamKey", streamKey);
            info.put("length", streamInfo.streamLength());
            info.put("radixTreeKeys", streamInfo.radixTreeKeySize());
            info.put("radixTreeNodes", streamInfo.radixTreeNodesSize());
            info.put("groups", streamInfo.groupCount());
            info.put("lastGeneratedId", streamInfo.lastGeneratedId());
            info.put("firstEntryId", streamInfo.getFirstEntry() != null ?
                    streamInfo.getFirstEntry().entrySet().iterator().next().getKey() : null);
            info.put("lastEntryId", streamInfo.getLastEntry() != null ?
                    streamInfo.getLastEntry().entrySet().iterator().next().getKey() : null);

            log.debug("스트림 {} 정보 조회 완료", streamKey);
            
        } catch (Exception e) {
            log.error("스트림 {} 정보 조회 중 오류 발생", streamKey, e);
            info.put("error", e.getMessage());
        }
        
        return info;
    }

    /**
     * 기본 스트림 정보 조회
     */
    public Map<String, Object> getDefaultStreamInfo() {
        return getStreamInfo(streamSettings.getStreamName());
    }

    // ==================== 기본 Redis 연산 ====================

    /**
     * Redis에 키-값 쌍을 저장하는 메서드 (TTL 설정 가능)
     * @param key 키
     * @param value 값
     * @param ttl TTL (Duration)
     */
    public void setWithTTL(String key, Object value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
            log.debug("키 {} 저장 완료 (TTL: {})", key, ttl);
        } catch (Exception e) {
            log.error("키 {} 저장 중 오류 발생", key, e);
            throw e;
        }
    }

    /**
     * Redis에서 값을 조회하는 메서드
     * @param key 키
     * @return 저장된 값
     */
    public Object get(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            log.debug("키 {} 조회 완료: {}", key, value);
            return value;
        } catch (Exception e) {
            log.error("키 {} 조회 중 오류 발생", key, e);
            throw e;
        }
    }

    /**
     * Redis Set에 값을 추가하는 메서드
     * @param key Set 키
     * @param values 추가할 값들
     * @return 추가된 원소의 수
     */
    public Long addToSet(String key, Object... values) {
        try {
            Long added = redisTemplate.opsForSet().add(key, values);
            log.debug("Set {}에 {}개 원소 추가", key, added);
            return added;
        } catch (Exception e) {
            log.error("Set {} 원소 추가 중 오류 발생", key, e);
            throw e;
        }
    }

    /**
     * Redis Set에서 값을 제거하는 메서드
     * @param key Set 키
     * @param values 제거할 값들
     * @return 제거된 원소의 수
     */
    public Long removeFromSet(String key, Object... values) {
        try {
            Long removed = redisTemplate.opsForSet().remove(key, values);
            log.debug("Set {}에서 {}개 원소 제거", key, removed);
            return removed;
        } catch (Exception e) {
            log.error("Set {} 원소 제거 중 오류 발생", key, e);
            throw e;
        }
    }

    /**
     * Redis Set의 모든 멤버를 조회하는 메서드
     * @param key Set 키
     * @return Set의 모든 멤버
     */
    public Set<Object> getSetMembers(String key) {
        try {
            Set<Object> members = redisTemplate.opsForSet().members(key);
            log.debug("Set {} 멤버 조회 완료: {}개", key, members != null ? members.size() : 0);
            return members;
        } catch (Exception e) {
            log.error("Set {} 멤버 조회 중 오류 발생", key, e);
            throw e;
        }
    }

    /**
     * Redis Set의 크기를 조회하는 메서드
     * @param key Set 키
     * @return Set의 크기
     */
    public Long getSetSize(String key) {
        try {
            Long size = redisTemplate.opsForSet().size(key);
            log.debug("Set {} 크기: {}", key, size);
            return size;
        } catch (Exception e) {
            log.error("Set {} 크기 조회 중 오류 발생", key, e);
            throw e;
        }
    }

    /**
     * 키가 존재하는지 확인하는 메서드
     * @param key 확인할 키
     * @return 존재 여부
     */
    public Boolean exists(String key) {
        try {
            Boolean exists = redisTemplate.hasKey(key);
            log.debug("키 {} 존재 여부: {}", key, exists);
            return exists;
        } catch (Exception e) {
            log.error("키 {} 존재 여부 확인 중 오류 발생", key, e);
            throw e;
        }
    }

    /**
     * 키를 삭제하는 메서드
     * @param key 삭제할 키
     * @return 삭제 여부
     */
    public Boolean delete(String key) {
        try {
            Boolean deleted = redisTemplate.delete(key);
            log.debug("키 {} 삭제: {}", key, deleted);
            return deleted;
        } catch (Exception e) {
            log.error("키 {} 삭제 중 오류 발생", key, e);
            throw e;
        }
    }

    /**
     * 키에 TTL을 설정하는 메서드
     * @param key 키
     * @param ttl TTL (Duration)
     * @return 설정 성공 여부
     */
    public Boolean expire(String key, Duration ttl) {
        try {
            Boolean success = redisTemplate.expire(key, ttl);
            log.debug("키 {} TTL 설정: {} ({})", key, success, ttl);
            return success;
        } catch (Exception e) {
            log.error("키 {} TTL 설정 중 오류 발생", key, e);
            throw e;
        }
    }

    // ==================== 채팅방 온라인 사용자 관리 ====================

    /**
     * 채팅방에 온라인 사용자를 추가
     * @param roomId 채팅방 ID
     * @param userId 사용자 ID
     */
    public void addOnlineUser(Long roomId, Long userId) {
        String key = "chat:room:" + roomId + ":online";
        addToSet(key, userId.toString());
        expire(key, Duration.ofMinutes(30)); // 30분 후 자동 만료
    }

    /**
     * 채팅방에서 온라인 사용자를 제거
     * @param roomId 채팅방 ID
     * @param userId 사용자 ID
     */
    public void removeOnlineUser(Long roomId, Long userId) {
        String key = "chat:room:" + roomId + ":online";
        removeFromSet(key, userId.toString());
    }

    /**
     * 채팅방의 온라인 사용자 수를 조회
     * @param roomId 채팅방 ID
     * @return 온라인 사용자 수
     */
    public Long getOnlineUserCount(Long roomId) {
        String key = "chat:room:" + roomId + ":online";
        return getSetSize(key);
    }

    /**
     * 채팅방의 온라인 사용자 목록을 조회
     * @param roomId 채팅방 ID
     * @return 온라인 사용자 ID 목록
     */
    public Set<Object> getOnlineUsers(Long roomId) {
        String key = "chat:room:" + roomId + ":online";
        return getSetMembers(key);
    }

    // ==================== 유틸리티 메서드 ====================

    /**
     * Redis 연결 상태를 테스트하는 메서드
     * @return 연결 상태 (true: 정상, false: 오류)
     */
    public boolean testConnection() {
        try {
            String testKey = "connection-test:" + System.currentTimeMillis();
            String testValue = "test-value";
            
            // 테스트 값 저장
            redisTemplate.opsForValue().set(testKey, testValue, Duration.ofSeconds(10));
            
            // 테스트 값 조회
            Object retrievedValue = redisTemplate.opsForValue().get(testKey);
            
            // 테스트 키 삭제
            redisTemplate.delete(testKey);
            
            boolean success = testValue.equals(retrievedValue);
            log.debug("Redis 연결 테스트 결과: {}", success);
            
            return success;
        } catch (Exception e) {
            log.error("Redis 연결 테스트 중 오류 발생", e);
            return false;
        }
    }

}