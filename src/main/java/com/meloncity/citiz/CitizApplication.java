package com.meloncity.citiz;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
//@RequiredArgsConstructor // RedisStreamService 제거로 인해 현재는 필요 없음 (필요 시 주석 해제)
@Slf4j
public class CitizApplication {

	// private final RedisStreamService redisStreamService;

	public static void main(String[] args) {
		SpringApplication.run(CitizApplication.class, args);
	}

	/**
	 * 애플리케이션이 완전히 시작된 후 실행되는 메서드
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationReady() {
		log.info("Citiz 애플리케이션이 시작되었습니다.");

		/* Kafka는 @KafkaListener에 의해 자동으로 컨슈머가 시작되므로 
		   기존 Redis Stream과 같은 별도의 시작 로직이 필요하지 않습니다.
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
		*/
	}
}
