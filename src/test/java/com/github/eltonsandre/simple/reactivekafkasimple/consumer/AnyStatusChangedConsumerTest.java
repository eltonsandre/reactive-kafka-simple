package com.github.eltonsandre.simple.reactivekafkasimple.consumer;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.eltonsandre.simple.reactivekafkasimple.dto.Any;
import com.github.eltonsandre.simple.reactivekafkasimple.dto.KafkaStatusEnum;
import lombok.RequiredArgsConstructor;
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
import org.springframework.util.SocketUtils;
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

@KafkaIntegrationTest
@Testcontainers
@DisplayName("Kafka Consumer - Function anyStatusChangedConsumer ")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class AnyStatusChangedConsumerTest {

    @Container
    static final KafkaContainer kafka;
    static final int port = SocketUtils.findAvailableTcpPort();

    static {
        kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"));
        kafka.start();
    }

    private final ReactiveKafkaProducerTemplate<String, Object> reactiveKafkaProducerTemplate;

    @SpyBean
    AnyStatusChangedConsumer anyStatusChangedConsumer;

    @Value("${spring.cloud.stream.bindings.anyStatusChangedConsumer-in-0.destination}")
    String topicName;

    @Captor
    ArgumentCaptor<Any> payloadCaptor;
    @Captor
    ArgumentCaptor<String> topicCaptor;
    @Captor
    ArgumentCaptor<String> correlationIdCaptor;

    @DynamicPropertySource
    static void dynamicKafkaProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @AfterAll
    void shutdown() {
        this.reactiveKafkaProducerTemplate.close();
        kafka.close();
    }

    @Test
    void consumer() throws JsonProcessingException {
        final var payloadExpected = Any.builder()
                .id("UUID.randomUUID().toString()")
                .name("Sarah Connor")
                .enabled(true)
                .status(KafkaStatusEnum.PROCESSING.name())
                .build();

        final var producerRecord = SenderRecord.<String, Object, String>create(this.topicName, 0,
                Instant.now().toEpochMilli(), payloadExpected.getId(), payloadExpected, UUID.randomUUID().toString());

        StepVerifier.create(this.reactiveKafkaProducerTemplate.send(producerRecord))
                .expectSubscription()
                .as("Quando existir mensagem no topico ")
                .assertNext(Assertions::assertNotNull)
                .verifyComplete();

        final VerificationMode times = Mockito.timeout(3000);
        verify(this.anyStatusChangedConsumer, times).apply(any());
        verify(this.anyStatusChangedConsumer, times)
                .processorMessage(this.topicCaptor.capture(), this.payloadCaptor.capture(), this.correlationIdCaptor.capture());

        Assertions.assertEquals(payloadExpected, this.payloadCaptor.getValue());
    }

}
