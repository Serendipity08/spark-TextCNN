package com.musicwise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MusicWiseApplication {
    public static void main(String[] args) {
        SpringApplication.run(MusicWiseApplication.class, args);
    }
}


















