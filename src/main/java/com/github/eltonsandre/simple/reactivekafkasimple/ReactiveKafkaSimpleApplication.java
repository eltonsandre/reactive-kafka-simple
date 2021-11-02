package com.github.eltonsandre.simple.reactivekafkasimple;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import reactor.blockhound.BlockHound;

@EnableAsync
@SpringBootApplication
public class ReactiveKafkaSimpleApplication {

    public static void main(String[] args) {
        BlockHound.install();
        SpringApplication.run(ReactiveKafkaSimpleApplication.class, args);
    }

}
