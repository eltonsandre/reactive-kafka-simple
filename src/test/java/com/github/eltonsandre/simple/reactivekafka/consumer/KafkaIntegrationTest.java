package com.github.eltonsandre.simple.reactivekafka.consumer;

import com.github.eltonsandre.simple.reactivekafka.configuration.KafkaConfiguration;
import com.github.eltonsandre.simple.reactivekafka.producer.ReactiveKafkaProducer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented

@Tag("integrationTest")
@Tag("kafka-consumer")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)

@ActiveProfiles("integration-test")
@SpringBootApplication
@EnableConfigurationProperties
@Import(KafkaConfiguration.class)
@SpringBootTest(classes = {KafkaIntegrationTest.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public @interface KafkaIntegrationTest {

    @AliasFor(attribute = "value", annotation = EnableConfigurationProperties.class)
    Class<?>[] configurationProperties() default {};

    @AliasFor(attribute = "properties", annotation = SpringBootTest.class)
    String[] properties() default {
            "spring.kafka.properties.sasl.mechanism=GSSAPI",
            "spring.kafka.properties.security.protocol=PLAINTEXT",
            "spring.cloud.stream.kafka.binder.configuration.sasl.mechanism=GSSAPI",
            "spring.cloud.stream.kafka.binder.configuration.security.protocol=PLAINTEXT"
    };

    @AliasFor(annotation = SpringBootApplication.class, attribute = "scanBasePackageClasses")
    Class<?>[] scanBasePackageClasses() default {AbstractReactiveKafkaConsumer.class, ReactiveKafkaProducer.class};

    @AliasFor(annotation = SpringBootApplication.class)
    Class<?>[] exclude() default {};

}