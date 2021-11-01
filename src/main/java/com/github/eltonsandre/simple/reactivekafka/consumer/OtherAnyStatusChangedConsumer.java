package com.github.eltonsandre.simple.reactivekafka.consumer;

import com.github.eltonsandre.simple.reactivekafka.dto.Any;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@KafkaConsumer
@RequiredArgsConstructor
public class OtherAnyStatusChangedConsumer extends AbstractReactiveKafkaConsumer.KeyDefault<Any> {

    @Override
    void processorMessage(final String topic, byte[] key, final Any payload, final String correlationId) {
        log.info("Consumer message: {}", payload);
    }

}
