package com.github.eltonsandre.simple.reactivekafka;

import com.github.eltonsandre.simple.reactivekafka.dto.Any;
import com.github.eltonsandre.simple.reactivekafka.producer.ReactiveKafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("producer")
@RequiredArgsConstructor
public class AnyResource {

    private final ReactiveKafkaProducer reactiveKafkaProducer;

    @PostMapping
    public Mono<Any> producerAny(@RequestBody Any any) {
        log.info("MDC: {}", MDC.getCopyOfContextMap());
        return this.reactiveKafkaProducer.send("any-status-changed", any.getId(), any).thenReturn(any);
    }

}
