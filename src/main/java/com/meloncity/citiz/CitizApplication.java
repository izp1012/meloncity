package com.meloncity.citiz;

import com.meloncity.citiz.service.RedisStreamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@RequiredArgsConstructor
@Slf4j
public class CitizApplication {

	private final RedisStreamService redisStreamService;

	public static void main(String[] args) {
		SpringApplication.run(CitizApplication.class, args);
	}

	/**
	 * 애플리케이션이 완전히 시작된 후 실행되는 메서드
	 * Redis Stream Consumer를 시작하고 Pending 메시지를 처리
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationReady() {
		log.info("채팅 애플리케이션이 시작되었습니다.");

		try {
			// Pending 메시지 처리 (애플리케이션 재시작 시 누락된 메시지 처리)
			redisStreamService.processPendingMessages();

			// Consumer Group 정보 로깅
			redisStreamService.logConsumerGroupInfo();

			// Redis Stream Consumer 시작 (비동기)
			redisStreamService.startConsumingMessages();

			log.info("Redis Stream Consumer가 시작되었습니다.");

		} catch (Exception e) {
			log.error("Redis Stream Consumer 시작 중 오류 발생", e);
			// 애플리케이션을 종료하지 않고 계속 실행 (다른 기능들은 정상 작동)
		}
	}
}
