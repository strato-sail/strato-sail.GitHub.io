package com.wcy.dubboservice.gmall;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

public class MainApplication {
    public static void main(String[] args) throws IOException {
        ClassPathXmlApplicationContext ioc = new ClassPathXmlApplicationContext("provider.xml");
        ioc.start();       //启动ioc容器
        System.in.read();  //不让程序停止
    }
}
