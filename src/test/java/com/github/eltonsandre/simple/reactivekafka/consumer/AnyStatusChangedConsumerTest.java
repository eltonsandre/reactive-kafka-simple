package com.github.eltonsandre.simple.reactivekafka.consumer;


import com.github.eltonsandre.simple.reactivekafka.dto.Any;
import com.github.eltonsandre.simple.reactivekafka.dto.KafkaStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.kafka.sender.SenderRecord;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@Slf4j
@KafkaIntegrationTest
@Testcontainers
@DisplayName("Kafka Consumer - Function anyStatusChangedConsumer ")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class AnyStatusChangedConsumerTest extends AbstractTestKafkaConsumer.KeyString<Any, AnyStatusChangedConsumer> {

    @DynamicPropertySource
    static void dynamicKafkaProperties(final DynamicPropertyRegistry registry) {
        dynamicKafkaProperties(registry, AnyStatusChangedConsumer.class, "topic-any");
    }

    @Test
    @DisplayName("Consumer - topic-any ")
    void consumer() {
        final var payloadExpected = Any.builder()
                .id(UUID.randomUUID().toString())
                .name("Sarah Connor")
                .enabled(true)
                .status(KafkaStatusEnum.PROCESSING.name())
                .build();

        assertAndVerifyTest("topic-any", payloadExpected.getId(), payloadExpected);
    }

}
