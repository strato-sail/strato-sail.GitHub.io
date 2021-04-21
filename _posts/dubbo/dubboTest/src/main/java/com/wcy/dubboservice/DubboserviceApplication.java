package com.wcy.dubboservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class DubboserviceApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(DubboserviceApplication.class, args);

    }

}
