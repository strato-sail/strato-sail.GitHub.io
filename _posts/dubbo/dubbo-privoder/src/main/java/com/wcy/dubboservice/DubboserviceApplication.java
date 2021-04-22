package com.wcy.dubboservice;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@EnableDubbo  //开启基于注解的dubbo功能
@SpringBootApplication
public class DubboserviceApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(DubboserviceApplication.class, args);

    }

}
