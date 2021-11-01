package com.github.eltonsandre.simple.reactivekafka.consumer;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.kafka.sender.SenderRecord;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@Slf4j
@Testcontainers
@KafkaIntegrationTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
public abstract class AbstractTestKafkaConsumer<KEY, MESSAGE_TYPE, KAFKA_CONSUMER extends AbstractReactiveKafkaConsumer<KEY, MESSAGE_TYPE>> {

    @Container
    protected static final KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))
            .withEmbeddedZookeeper();

    protected final VerificationMode verificationMode = Mockito.timeout(5000);

    protected final RecordHeaders headers = new RecordHeaders();

    @Autowired
    private ReactiveKafkaProducerTemplate<String, Object> reactiveKafkaProducerTemplate;

    @SpyBean
    protected KAFKA_CONSUMER kafkaConsumer;

    @Captor
    protected ArgumentCaptor<String> topicCaptor;
    @Captor
    protected ArgumentCaptor<String> correlationIdCaptor;
    @Captor
    protected ArgumentCaptor<KEY> keyCaptor;
    @Captor
    protected ArgumentCaptor<MESSAGE_TYPE> payloadCaptor;

    static void dynamicKafkaProperties(final DynamicPropertyRegistry registry,
                                       final @NotNull Class<? extends AbstractReactiveKafkaConsumer<?, ?>> clazz,
                                       final String topicName) {

        final String consumerBinding = StringUtils.uncapitalize(clazz.getSimpleName());
        registry.add("spring.cloud.stream.function.definition", () -> consumerBinding);
        registry.add("spring.cloud.stream.bindings." + consumerBinding + "-in-0.destination", () -> topicName);

        kafkaContainer.start();

        registry.add("spring.cloud.stream.kafka.binder.brokers", () -> {
            final String bootstrapServers = kafkaContainer.getBootstrapServers();
            log.info("kafkaContainer, start, bootstrapServers: {}", bootstrapServers);
            return bootstrapServers;
        });
    }

    @AfterAll
    void shutdown() {
        this.reactiveKafkaProducerTemplate.close();
        kafkaContainer.close();
    }

    @SneakyThrows
    public void assertAndVerifyTest(final String topicName, final String key, final MESSAGE_TYPE payloadExpected) {
        this.sendMessage(topicName, key, payloadExpected);

        final KAFKA_CONSUMER verifyConsumer = verify(this.kafkaConsumer, this.verificationMode);

        verifyConsumer.processorMessage(this.topicCaptor.capture(), this.keyCaptor.capture(),
                this.payloadCaptor.capture(), this.correlationIdCaptor.capture());

        Assertions.assertAll("Então deverá ser consumido a mensagem do topico " + topicName,
                () -> Assertions.assertEquals(payloadExpected, this.payloadCaptor.getValue()),
                () -> Assertions.assertEquals(topicName, this.topicCaptor.getValue()));
    }

    protected void sendMessage(final String topicName, final String key, final MESSAGE_TYPE payloadExpected) {
        final var producerRecord = new ProducerRecord<String, Object>(topicName, null, Instant.now().toEpochMilli(), key, payloadExpected);
        headers.forEach(header -> producerRecord.headers().add(header));
        final var senderRecord = SenderRecord.create(producerRecord, UUID.randomUUID().toString());

        StepVerifier.create(this.reactiveKafkaProducerTemplate.send(senderRecord))
                .expectSubscription()
                .as("Quando existir mensagem no topico ")
                .assertNext(it -> Assertions.assertEquals(topicName, it.recordMetadata().topic()))
                .verifyComplete();
    }

    public abstract static class KeyString<MESSAGE_TYPE, KAFKA_CONSUMER
            extends AbstractReactiveKafkaConsumer<String, MESSAGE_TYPE>>
            extends AbstractTestKafkaConsumer<String, MESSAGE_TYPE, KAFKA_CONSUMER> {
    }

}
