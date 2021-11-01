package com.github.eltonsandre.simple.reactivekafka.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.BytesSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.binder.kafka.properties.KafkaBinderConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import reactor.kafka.sender.SenderOptions;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@EnableKafka
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({KafkaBinderConfigurationProperties.class})
public class KafkaConfiguration {

    private final KafkaBinderConfigurationProperties kafkaProperties;

    @Bean
    public ReactiveKafkaProducerTemplate<String, Object> reactiveKafkaProducerTemplate(final ObjectMapper objectMapper) {
        final Map<String, Object> producerProperties = new HashMap<>(this.kafkaProperties.mergedProducerConfiguration());
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        final SenderOptions<String, Object> senderOptions = SenderOptions.<String, Object>create(producerProperties)
                .withValueSerializer(new JsonSerializer<>(objectMapper));

        return new ReactiveKafkaProducerTemplate<>(senderOptions);
    }

}
