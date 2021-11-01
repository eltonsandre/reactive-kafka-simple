package com.github.eltonsandre.simple.reactivekafka.consumer;

import com.github.eltonsandre.simple.reactivekafka.dto.KafkaStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.instrument.messaging.MessagingSleuthOperators;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.util.UUID;
import java.util.function.Function;

/**
 * @param <K> Kafka message key type
 * @param <T> Kafka message value type
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractReactiveKafkaConsumer<K, T> implements Function<Flux<Message<T>>, Mono<Void>> {

    private static final String LOG_MESSAGE = "Consumer Received, topic: {}, status: {}, correlationId: {}, partitionId: {}, offSet: {}"
            .concat(log.isDebugEnabled() ? ", payload: {}" : "");

    @Autowired
    BeanFactory beanFactory;

    abstract void processorMessage(final String topic, K key, final T payload, final String correlationId);

    @Override
    public Mono<Void> apply(final Flux<Message<T>> messageFlux) {
        return messageFlux
                .map(message -> MessagingSleuthOperators.asFunction(this.beanFactory, message).apply(message))
                .doOnNext(this::beforeProcessor)
                .doOnError(it -> log.error("kafkaConsumer, topic: {}, status: ERROR, message: {}", it, it))
                .retryWhen(Retry.max(1).transientErrors(true))
                .onErrorContinue((ex, it) -> log.error("kafkaConsumer, payload: {}, status: ERROR, message: {}", it, ex))
                .repeat()
                .then();
    }

    protected void beforeProcessor(Message<T> message) {
        final String kafkaCorrelationId = message.getHeaders().get(KafkaHeaders.CORRELATION_ID, String.class);
        final String correlationId = StringUtils.hasText(kafkaCorrelationId) ? kafkaCorrelationId : UUID.randomUUID().toString();

        final Long offSet = message.getHeaders().get(KafkaHeaders.OFFSET, Long.class);
        final String topic = message.getHeaders().get(KafkaHeaders.RECEIVED_TOPIC, String.class);
        final Integer partitionId = message.getHeaders().get(KafkaHeaders.RECEIVED_PARTITION_ID, Integer.class);

        final byte[] messageKeyBytes = message.getHeaders().get(KafkaHeaders.RECEIVED_MESSAGE_KEY, byte[].class);
        final K key = ArrayUtils.isNotEmpty(messageKeyBytes) ? this.parseMessageKey().apply(messageKeyBytes) : null;

        log.info(LOG_MESSAGE, topic, KafkaStatusEnum.START, correlationId, partitionId, offSet, message.getPayload());

        this.processorMessage(topic, key, message.getPayload(), correlationId);

        log.info(LOG_MESSAGE, topic, KafkaStatusEnum.SUCCESS, correlationId, partitionId, offSet, message.getPayload());
    }

    abstract Function<byte[], K> parseMessageKey();

    public abstract static class KeyString<T> extends AbstractReactiveKafkaConsumer<String, T> {

        protected Function<byte[], String> parseMessageKey() {
            return String::new;
        }

    }
    public abstract static class KeyDefault<T> extends AbstractReactiveKafkaConsumer<byte[], T> {

        protected Function<byte[], byte[]> parseMessageKey() {
            return it -> it;
        }

    }

}
