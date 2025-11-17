package com.kaddy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MedicoApplication {
    public static void main(String[] args) {
        SpringApplication.run(MedicoApplication.class, args);
    }
}
