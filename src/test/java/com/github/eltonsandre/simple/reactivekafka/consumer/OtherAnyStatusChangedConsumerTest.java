package com.github.eltonsandre.simple.reactivekafka.consumer;


import com.github.eltonsandre.simple.reactivekafka.dto.Any;
import com.github.eltonsandre.simple.reactivekafka.dto.KafkaStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@KafkaIntegrationTest
@Testcontainers
@DisplayName("Kafka Consumer - Function otherAnyStatusChangedConsumer ")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class OtherAnyStatusChangedConsumerTest extends AbstractTestKafkaConsumer<byte[], Any, OtherAnyStatusChangedConsumer> {

    @DynamicPropertySource
    static void dynamicKafkaProperties(final DynamicPropertyRegistry registry) {
        dynamicKafkaProperties(registry, OtherAnyStatusChangedConsumer.class, "topic-other");
    }

    static Stream<Arguments> methodSourceParams() {
        return Stream.of(
                Arguments.of(UUID.randomUUID().toString(), "John Connor",KafkaStatusEnum.START, false),
                Arguments.of(UUID.randomUUID().toString(), "Sarah Connor",KafkaStatusEnum.PROCESSING, false),
                Arguments.of(UUID.randomUUID().toString(), "Other Connor", KafkaStatusEnum.SUCCESS, true)
        );
    }

    @MethodSource("methodSourceParams")
    @ParameterizedTest(name = "Consumer - topic-any: name {1}, status: {2}")
    void consumer(String uuid, String name, KafkaStatusEnum kafkaStatusEnum,  boolean enabled) {
        final var payloadExpected = Any.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .enabled(enabled)
                .status(kafkaStatusEnum.name())
                .build();

        assertAndVerifyTest("topic-other", payloadExpected.getId(), payloadExpected);

        Assertions.assertEquals(payloadExpected.getId(), new String(this.keyCaptor.getValue()));

    }

}
