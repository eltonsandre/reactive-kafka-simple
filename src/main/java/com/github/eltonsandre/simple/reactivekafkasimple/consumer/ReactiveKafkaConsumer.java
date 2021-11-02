package com.github.eltonsandre.simple.reactivekafkasimple.consumer;

import com.github.eltonsandre.simple.reactivekafkasimple.dto.KafkaStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.instrument.messaging.MessagingSleuthOperators;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public abstract class ReactiveKafkaConsumer<T> implements
        java.util.function.Function<Flux<Message<T>>, Mono<Void>> {

    private static final String LOG_MESSAGE = "Consumer Received, topic: {}, status: {}, correlationId: {}, partitionId: {}, offSet: {}"
            .concat(log.isDebugEnabled() ? ", payload: {}" : "");

    @Autowired
    BeanFactory beanFactory;

    abstract void processorMessage(final String topic, final T payload, final String correlationId);

    @Override
    public Mono<Void> apply(final Flux<Message<T>> messageFlux) {
        return messageFlux
                .map(message -> MessagingSleuthOperators.asFunction(this.beanFactory, message).apply(message))
                .doOnNext(this::beforeProcessor)
                .then();
    }

    protected void beforeProcessor(Message<T> message) {
        final String kafkaCorrelationId = message.getHeaders().get(KafkaHeaders.CORRELATION_ID, String.class);
        final String correlationId = StringUtils.hasText(kafkaCorrelationId) ? kafkaCorrelationId : UUID.randomUUID().toString();

        final Long offSet = message.getHeaders().get(KafkaHeaders.OFFSET, Long.class);
        final String topic = message.getHeaders().get(KafkaHeaders.RECEIVED_TOPIC, String.class);
        final Integer partitionId = message.getHeaders().get(KafkaHeaders.RECEIVED_PARTITION_ID, Integer.class);

        log.info(LOG_MESSAGE, topic, KafkaStatusEnum.START, correlationId, partitionId, offSet, message.getPayload());

        this.processorMessage(topic, message.getPayload(), correlationId);

        log.info(LOG_MESSAGE, topic, KafkaStatusEnum.SUCCESS, correlationId, partitionId, offSet, message.getPayload());
    }

}
