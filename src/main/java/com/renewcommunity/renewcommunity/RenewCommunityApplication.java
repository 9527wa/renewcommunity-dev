package com.renewcommunity.renewcommunity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class RenewCommunityApplication {

    public static void main(String[] args) {
        SpringApplication.run(RenewCommunityApplication.class, args);
    }

}
