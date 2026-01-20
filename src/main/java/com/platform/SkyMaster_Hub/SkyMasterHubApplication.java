package com.platform.SkyMaster_Hub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
// @EnableCaching
public class SkyMasterHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkyMasterHubApplication.class, args);
    }

}
