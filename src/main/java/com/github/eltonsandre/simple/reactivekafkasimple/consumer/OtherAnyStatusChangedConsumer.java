package com.github.eltonsandre.simple.reactivekafkasimple.consumer;

import com.github.eltonsandre.simple.reactivekafkasimple.dto.Any;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@KafkaConsumer
@RequiredArgsConstructor
public class OtherAnyStatusChangedConsumer extends ReactiveKafkaConsumer<Any> {

    @Override
    void processorMessage(final String topic, final Any payload, final String correlationId) {
        log.info("Consumer message: {}", payload);
    }

}
