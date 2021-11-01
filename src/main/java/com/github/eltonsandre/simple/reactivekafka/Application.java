package com.github.eltonsandre.simple.reactivekafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
//import reactor.blockhound.BlockHound;

@EnableAsync
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
//        BlockHound.install();
        SpringApplication.run(Application.class, args);
    }

}
