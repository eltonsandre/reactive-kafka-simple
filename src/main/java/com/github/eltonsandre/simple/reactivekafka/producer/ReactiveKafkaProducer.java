package com.github.eltonsandre.simple.reactivekafka.producer;

import brave.Tracer;
import com.github.eltonsandre.simple.reactivekafka.dto.KafkaStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderRecord;
import reactor.kafka.sender.SenderResult;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static com.github.eltonsandre.simple.reactivekafka.dto.KafkaStatusEnum.SUCCESS;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReactiveKafkaProducer {

    private final Tracer tracer;
    private final ReactiveKafkaProducerTemplate<String, Object> kafkaProducer;

    public Mono<SenderResult<String>> send(final String topicName, final String key, final Object payload) {
        return this.send(new ProducerRecord<>(topicName, null, Instant.now().toEpochMilli(), key, payload));
    }

    public Mono<SenderResult<String>> send(final ProducerRecord<String, Object> producerRecord) {
        final var correlationId = this.tracer.currentSpan().context().traceIdString();
        return this.send(SenderRecord.create(producerRecord, correlationId));
    }

    public Mono<SenderResult<String>> send(final SenderRecord<String, Object, String> senderRecord) {
        return this.kafkaProducer.send(senderRecord)
                .doOnSuccess(senderResult -> {
                    final var metadata = senderResult.recordMetadata();
                    log.info("kafkaProducer, topic: {}, status: {}, correlationId: {}, partition: {}, offset:{} timestamp: {}",
                            metadata.topic(), SUCCESS, senderResult.correlationMetadata(),
                            metadata.partition(), metadata.offset(), metadata.timestamp());
                })
                .doOnNext(it -> log.info("kafkaProducer, topic: {}, status: {},correlationId: {}, partition: {}", SUCCESS,
                        it.recordMetadata().topic(), senderRecord.correlationMetadata(), it.recordMetadata().partition()))
                .doOnError(e -> log.error("kafkaProducer, topic: {}, status: {}, correlationId: {},",
                        senderRecord.topic(), KafkaStatusEnum.ERROR, senderRecord.correlationMetadata(), e));
    }

}