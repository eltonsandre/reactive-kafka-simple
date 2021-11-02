package com.github.eltonsandre.simple.reactivekafkasimple.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import reactor.kafka.sender.SenderOptions;

import java.util.Map;

@Slf4j
@EnableKafka
@Configuration
@RequiredArgsConstructor
public class KafkaConfiguration {

    private final KafkaProperties kafkaProperties;

    @Bean
    public ReactiveKafkaProducerTemplate<String, Object> reactiveKafkaProducerTemplate(final ObjectMapper objectMapper) {
        final Map<String, Object> producerProperties = this.kafkaProperties.buildProducerProperties();

        final SenderOptions<String, Object> senderOptions = SenderOptions.<String, Object>create(producerProperties)
                .withKeySerializer(new StringSerializer())
                .withValueSerializer(new JsonSerializer<>(objectMapper));

        return new ReactiveKafkaProducerTemplate<>(senderOptions);
    }

}
