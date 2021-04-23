package com.wcy.dubboservice.gmall.config;

import com.alibaba.dubbo.config.*;
import com.alibaba.dubbo.registry.Registry;
import com.wcy.dubbotestinterface.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class MyDubboConfig {

    //<!-- 1. 指定当前服务/应用的名字（同样的服务名字相同，不要和别的服务同名） -->
    //<dubbo:application name="dubboTest"></dubbo:application>
    @Bean
    public ApplicationConfig applicationConfig(){
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setName("user-service-provider");
        return applicationConfig;
    }

    //<!-- 2. 指定注册中心的位置 -->
    //<dubbo:registry address="zookeeper://127.0.0.1:2181"></dubbo:registry>
    @Bean
    public RegistryConfig registryConfig(){
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress("localhost:2181");
        registryConfig.setProtocol("zookeeper");
        return registryConfig;
    }

    //<!-- 3. 指定通信规则（通信协议，通信端口） -->
    //<dubbo:protocol name="dubbo" port="20080"></dubbo:protocol>
    @Bean
    public ProtocolConfig protocolConfig(){
        ProtocolConfig protocolConfig = new ProtocolConfig();
        protocolConfig.setName("dubbo");
        protocolConfig.setPort(20880);
        return protocolConfig;
    }

    //<!-- 4. 暴露服务 interface接口全限定类型 ref指向服务真正的实现对象-->
    //    <dubbo:service interface="com.wcy.dubbotestinterface.service.UserService" ref = "userServiceImpl">
    //        <dubbo:method name = "getUserAddressList" timeout="1000"/>
    //    </dubbo:service>
    @Bean
    public ServiceConfig<UserService> userServiceConfig(UserService userService){  //springboot自动将参数注入
        ServiceConfig<UserService> userServiceConfig = new ServiceConfig<>();
        userServiceConfig.setInterface(UserService.class);
        userServiceConfig.setRef(userService);

        //配置每个method的信息
        MethodConfig methodConfig = new MethodConfig();
        methodConfig.setName("getUserAddressList");
        methodConfig.setTimeout(1000);

        //将method的设置关联到service配置中
        List<MethodConfig> methods = new ArrayList<>();
        methods.add(methodConfig);
        userServiceConfig.setMethods(methods);

        return userServiceConfig;
    }

    //ProviderConfig
    //MonitorConfig

}
