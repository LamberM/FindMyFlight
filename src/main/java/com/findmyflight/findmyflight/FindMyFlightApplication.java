package com.findmyflight.findmyflight;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FindMyFlightApplication {

    public static void main(String[] args) {
        SpringApplication.run(FindMyFlightApplication.class, args);
    }

}
