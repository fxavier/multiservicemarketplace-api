package com.xavier.multiservicemarketplaceapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MultiservicemarketplaceApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultiservicemarketplaceApiApplication.class, args);
    }

}
