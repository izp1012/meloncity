package com.meloncity.citiz.config.kafka;

import com.meloncity.citiz.dto.ChatMessageDto;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${app.kafka.consumer.max-poll-records:10}")
    private int maxPollRecords;

    @Value("${app.kafka.consumer.fetch-max-wait-ms:2000}")
    private int fetchMaxWaitMs;

    // Producer 설정
    @Bean
    public ProducerFactory<String, ChatMessageDto> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, ChatMessageDto> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // Consumer 설정
    @Bean
    public ConsumerFactory<String, ChatMessageDto> consumerFactory() {
        JsonDeserializer<ChatMessageDto> deserializer = new JsonDeserializer<>(ChatMessageDto.class);
        deserializer.setRemoveTypeHeaders(false);
        deserializer.addTrustedPackages("com.meloncity.citiz.dto");
        deserializer.setUseTypeMapperForKey(true);

        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer);
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        config.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, fetchMaxWaitMs);

        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ChatMessageDto> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ChatMessageDto> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}
