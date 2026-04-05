package com.creditapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CreditAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(CreditAppApplication.class, args);
    }

}
