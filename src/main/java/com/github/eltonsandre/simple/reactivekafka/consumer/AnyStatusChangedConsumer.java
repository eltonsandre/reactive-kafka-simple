package com.github.eltonsandre.simple.reactivekafka.consumer;

import com.github.eltonsandre.simple.reactivekafka.dto.Any;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
//@KafkaConsumer("anyStatusChangedConsumer")
@KafkaConsumer
@RequiredArgsConstructor
public class AnyStatusChangedConsumer extends AbstractReactiveKafkaConsumer.KeyString<Any> {

    @Override
    void processorMessage(final String topic, String key, final Any payload, final String correlationId) {
        log.info("Consumer topic {}, payload: {}, correlationId: {}", topic, payload, correlationId);
    }

}
