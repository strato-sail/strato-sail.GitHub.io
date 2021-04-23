package com.wcy.dubboservice;

import com.alibaba.dubbo.config.spring.context.annotation.DubboComponentScan;
import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ImportResource;

//@EnableDubbo  //开启基于注解的dubbo功能
//@ImportResource(locations = "classpath:provider.xml")
@EnableDubbo(scanBasePackages = "com.wcy.dubboservice.gmall")
@SpringBootApplication
public class DubboserviceApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(DubboserviceApplication.class, args);

    }

}
