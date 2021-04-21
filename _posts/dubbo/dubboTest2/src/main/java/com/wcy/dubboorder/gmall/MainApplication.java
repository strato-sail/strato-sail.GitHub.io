package com.wcy.dubboorder.gmall;

import com.wcy.dubbotestinterface.service.OrderService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

public class MainApplication {
    public static void main(String[] args) throws IOException {
        ClassPathXmlApplicationContext ioc = new ClassPathXmlApplicationContext("consumer.xml");
        OrderService orderService = ioc.getBean(OrderService.class);
        orderService.initOrder("1");
        System.out.println("调用完成");
        System.in.read();
    }
}
