---
layout:     post
title:      "SpringCloud 使用学习笔记"
subtitle:   "Learn about spring cloud"
date:       2021-04-27 12:00:00
author:     "Wangcy"
catalog: false
header-style: text
tags:
  - spring 
  - springCloud
  - 分布式
  - 微服务
  - Eureka
  - Zookeeper
  - Consul
---



# 简介

**SpringCloud**（[官网地址](https://spring.io/projects/spring-cloud)）是分布式微服务架构的一站式解决方案，是多种微服务架构落地技术的集合体，俗称微服务全家桶。

**springCloud和springBoot版本适配关系：**

![版本适配关系](\img\in-post\springcloud\springcloud-01.png)

详情见：[spring cloud2020.0.2 + spring boot2.4.2官方文档](https://docs.spring.io/spring-cloud/docs/current/reference/html/)



# 服务注册中心



## Eureka 服务注册与发现(停更)

**1）Eureka 概念**

下图是 Eureka 的架构图：

![](\img\in-post\springcloud\springcloud-02.png)

- Eureka采用了**CS的设计架构**，Eureka Server作为服务注册功能的服务器，它是服务注册中心。而系统中的其他微服务，使用Eureka的客户端连接到Eureka Sever并维持心跳连接。这样系统的维护人员就可以通过Eureka Server来监控系统中各个微服务是否正常运行。
- Eureka有两个组件：Eureka Server 和 Eureka Client
  - Eureka Server 提供服务注册服务，Eureka Server 中的**服务注册表**会储存所有可用节点的信息。
  - Eureka Client 通过注册中心进行访问，应用会向Eureka Server发送**心跳**，无心跳连接会移除服务注册表。有一个内置的轮询算法的**负载均衡器**。

- 服务提供者是一个集群，Eureka服务器也是一个集群
- 在服务注册与发现中，有一个注册中心。当服务器启动的时候，会把当前自己服务器的信息比加如服务地址**通讯地址等以别名方式注册到注册中心**上。另一方(消费者服务提供者)，以该别名的方式**去注册中心上获取到实际的服务通讯地址**，然后再实现本地RPC调用RPC远程调用
- **框架核心设计思想：在于注册中心**，因为使用注册中心管理每个服务与服务之间的一个依赖关系(服务治理概念)。在任何rpc远程框架中，都会有一个注册中心(存放服务地址相关信息(接口地址)



**对比Dubbo有相似的架构：**[Dubbo 学习链接](https://strato-sail.github.io/2021/04/21/dubbo/)



**2）单机版的 eureka**

**步骤1：**引入pom依赖

~~~xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>cloudstudy</artifactId>
        <groupId>com.wcy.springcloud</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>cloud-eureka-server7001</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
    </dependencies>
</project>
~~~

**步骤2：**配置文件

~~~yaml
server:
  port: 7001

eureka:
  instance:
    hostname: localhost  # eureka服务端的实例名称
  client:
    # false表示不向注册中心注册自己
    register-with-eureka: false
    # false表示自己端就是注册中心，我的职责就是维护服务实例，并不需要去检索服务
    fetch-registry: false
    service-url:
      # 设置与Eureka Server交互的地址查询服务和注册服务都需要依赖这个地址
      defaultZone: http://${eureka.instance.hostname}:${server.port}
~~~

**步骤3：**主启动类

~~~java
@SpringBootApplication
@EnableEurekaServer   //启动eureka服务端
public class EurekaMain7001 {
    public static void main(String[] args) {
        SpringApplication.run(EurekaMain7001.class, args);
    }
}
~~~

**步骤4：**可以启动当前项目，并访问 http://localhost:7001 查看web端的注册中心。

**步骤5：**将服务提供者和消费者注册到eureka

添加依赖：

~~~xml
<dependency>
	<groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    <version>2.2.1.RELEASE</version>
</dependency>
~~~

更改配置：

~~~yaml
spring:
  application:
    name: cloud-payment-service  # 微服务的名称！！
eureka:
  client:
    # 表示是否将自己注册进EurekaServer默认为ture
    register-with-eureka: true
    # 是否从EurekaServer抓取已有的注册信息，默认为true。
    # 单节点无所谓，集群必须设置为true才能配合ribbon使用负载均衡
    fetchRegistry: true
    service-url:
      defaultZone: http://localhost:7001/eureka
~~~

主启动类：添加`@EnableEurekaClient`注解进行注册

测试：先启动服务注册中心，然后再启动客户端，就能在 http://localhost:7001/ 看到已经注册的服务。



**3）集群版的 eureka**

为了高可用使用集群版，实现负载均衡+故障容错。

- 需要注册中心集群化和服务提供者集群化。
- 注册中心**互相注册，相互守望**

**步骤1：**将不同的server名字更改

在单机模拟的时候，改本机host文件

![](\img\in-post\springcloud\springcloud-03.png)

**步骤2：**改 7001 的配置文件

~~~yaml
server:
  port: 7001

eureka:
  instance:
    hostname: eureka7001.com  # eureka服务端的实例名称
  client:
    # false表示不向注册中心注册自己
    register-with-eureka: false
    # false表示自己端就是注册中心，我的职责就是维护服务实例，并不需要去检索服务
    fetch-registry: false
    service-url:
      # 设置与Eureka Server交互的地址查询服务和注册服务都需要依赖这个地址
      defaultZone: http://eureka7002.com:7002/eureka/
~~~

7002也一样的，`port`和`defaultZone`和 7001 相反。

**步骤3：**启动 server7001 和 server7002，成功启动后 http://eureka7001.com:7001/ 有复制冗余了

![](\img\in-post\springcloud\springcloud-04.png)

**步骤4：**将服务提供者的配置更改

~~~yaml
eureka:
  client:
    # 表示是否将自己注册进EurekaServer默认为ture
    register-with-eureka: true
    # 是否从EurekaServer抓取已有的注册信息，默认为true。单节点无所谓，集群必须设置为true才能配合ribbon使用负载均衡
    fetchRegistry: true
    service-url:
    # 俩台server！！
      defaultZone: http://eureka7001.com:7001/eureka,http://eureka7002.com:7002/eureka
~~~

**步骤5：**测试先启动EurekaServer7001/7002，然后启动服务提供者8001，然后启动服务消费者80，访问 http://localhost/consumer/payment/get/31 。

**步骤6：**服务提供者集群化配置，增加可用性

模仿 `cloud-provider-payment8001` 这个项目，创建`payment8002`

**步骤7：**改服务调用者的 `Controler`，将访问的 `url` 改为注册中心注册的服务名：

~~~java
    //private static final String PAYMENT_URL = "http://localhost:8001";
    private static final String PAYMENT_URL = "http://CLOUD-PAYMENT-SERVICE";
~~~

<u>此时重启调用服务会出错！</u>因为`restTemplate`现在知道了服务提供者集群的名字，但是并不知道调用集群中的哪一台服务器，于是应该在`restTemplate`的`Bean`中加上`@LoadBalance`注解，默认采用轮询的方式：

~~~java
@Configuration
public class ApplicationContextConfig {
    @Bean
    @LoadBalanced  //使用这个注解赋予RestTemplate负载均衡的能力
    public RestTemplate getRestTemplate(){
        return new RestTemplate();
    }
}
~~~

**步骤8：**可以将注册中心的服务提供者名字改了，修改yml配置文件：

~~~yaml
eureka:
  client:
    # 表示是否将自己注册进EurekaServer默认为ture
    register-with-eureka: true
    # 是否从EurekaServer抓取已有的注册信息，默认为true。单节点无所谓，集群必须设置为true才能配合ribbon使用负载均衡
    fetchRegistry: true
    service-url:
      # defaultZone: http://localhost:7001/eureka
      defaultZone: http://eureka7001.com:7001/eureka,http://eureka7002.com:7002/eureka
  instance:
    instance-id: payment8002 # 注册中心改名字
    prefer-ip-address: true # 访问路径可以显示IP地址
~~~

**步骤9：DiscoveryClient** 对于注册进`eureka`里面的微服务，可以通过服务发现来获得该服务的信息：

1. `controller`自动注入

   ```java
   @Autowired
   private DiscoveryClient discoveryClient;
   ```

2. `controller` 加入映射

   ```java
   @GetMapping(value = "/payment/discovery")
       public Object discovery() {
           List<String> services = discoveryClient.getServices();
           for (String element : services) {
               log.info("element:" + element);
           }
           List<ServiceInstance> instances = discoveryClient.getInstances("CLOUD-PAYMENT-SERVICE");
           for (ServiceInstance instance : instances) {
               log.info(instance.getInstanceId() + "\t" + instance.getHost() + "\t" + instance.getPort() + "\t" + instance.getUri());
           }
           return this.discoveryClient;
       }
   ```

3. 主启动类加入注解

   ```java
   @SpringBootApplication
   @EnableEurekaClient
   @EnableDiscoveryClient   //启动服务发现
   public class PaymentMain8001 {
       public static void main(String[] args) {
           SpringApplication.run(PaymentMain8001.class, args);
       }
   }
   ```

**步骤10：**Eureka 自我保护模式

- 保护模式主要用于一组客户端和Eureka Server之间存在网络分区场景下的保护。
- 一旦进入保护模式，Eureka Server 就会尝试保护其服务注册表中的信息，不再删除服务注册表中的数据，也就是**不会注销任何微服务**。
- 某时刻某一个微服务不可用了，Eureka不会立即清理，依旧会对该微服务的信息进行保存。
- 属于CAP里面的AP分支
- 为了防止 EurekaClient 可以正常运行，但是与 EurekaServer 网络不通情况下，EurekaServer 不会立刻将 EurekaClient 服务剔除
- 默认情况下，如果EurekaServer在一定时间内没有接收到某个微服务实例的心跳，EurekaServer将会注销该实例（默认90秒)。但是当网络分区故障发生(延时、卡顿、拥挤)时，微服务与EurekaSerser之间无法正常通信，以上行为可能变得非常危险了——因为微服务本身其实是健康的，**此时本不应该注销这个微服务。**Eureka通过“自我保护模式”来解决这个问题——当EurekaServer节点在短时间内丢失过多客户端时（可能发生了网络分区故障)，那么这个节点就会进入自我保护模式。

**禁止自我保护的方式：**

- 出厂默认，自我保护机制是开启的，修改注册中心 7001 的配置

  ~~~properties
  eureka.server.enable-self-preservation = false      # 禁用自我保护模式
  eureka.server.eviction-interval-timer-in-ms = 2000  # 设置接受心跳时间间隔
  ~~~

- 修改服务提供者 8001 的配置

  ~~~properties
  # Eureka服务端在收到最后一次心的秒(默认是90秒)，超时将剔除服务
  eureka.instance.lease-expiration-duration-in-seconds: 90  
  # Eureka客户端向服务端发送心跳的时间间隔，单位为秒（默认是30秒）
  eureka.instance.lease-renewal-interval-in-seconds: 30     
  ~~~

  

## Zookeeper服务注册中心

*简介：SpringCloud整合Zookeeper代替Eureka*

zookeeper 是一个分布式协调工具，可以实现注册中心功能

**1）服务提供者**

**步骤1：**POM依赖

~~~xml
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-zookeeper-discovery</artifactId>
</dependency>
~~~

**步骤2：**YML配置文件

~~~yml
# 8004代表注册到zookeeper服务器的支付服务提供者端口号
server:
  port: 8004

# 服务别名---注册zookeeper到服务中心名称
spring:
  application:
    name: cloud-provider-payment
  cloud:
    zookeeper:
      connect-string: 192.168.1.150:2181
~~~

**步骤3：**主启动类

~~~java
@SpringBootApplication
@EnableDiscoveryClient  //用于向使用consul或者zookeeper作为注册中心时注册服务
public class PaymentMain8004 {
    public static void main(String[] args) {
        SpringApplication.run(PaymentMain8004.class, args);
    }
}
~~~

**步骤4：**controller

~~~java
@RestController
@Slf4j
public class PaymentController {
    @Value("server.port")
    private String serverPort;

    @RequestMapping(value = "/payment/zk")
    public String paymentzk() {
        return "springcloud with zookeeper:" + serverPort + "\t" + UUID.randomUUID().toString();
    }
}
~~~

**步骤5：**测试，启动8004注册进zookeeper

1. zookeeper根节点下出现了services新节点，services节点下有个cloud-provider-payment节点，就是步骤2中自定义的服务名。**zookeeper的服务节点是 -e 临时的**

   ~~~shell
   [zk: localhost:2181(CONNECTED) 12] get /services/cloud-provider-payment/0f0a2cbc-4ba3-4ffa-9fcc-4daa8701a320 
   {"name":"cloud-provider-payment","id":"0f0a2cbc-4ba3-4ffa-9fcc-4daa8701a320","address":"localhost","port":8004,"sslPort":null,"payload":{"@class":"org.springframework.cloud.zookeeper.discovery.ZookeeperInstance","id":"application-1","name":"cloud-provider-payment","metadata":{}},"registrationTimeUTC":1620286926493,"serviceType":"DYNAMIC","uriSpec":{"parts":[{"value":"scheme","variable":true},{"value":"://","variable":false},{"value":"address","variable":true},{"value":":","variable":false},{"value":"port","variable":true}]}}
   ~~~

2. 进入链接 http://localhost:8004/payment/zk 测试。



**2）服务消费者**

**步骤1：**POM文件

~~~xml
<!--zookeeper-->
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-zookeeper-discovery</artifactId>
</dependency>
~~~

**步骤2：**YAML配置文件

~~~yaml
server:
  port: 80

spring:
  application:
    name: cloud-consumer-order
  cloud:
    # 注册到zookeeper地址
    zookeeper:
      connect-string: 192.168.1.150:2181
~~~

**步骤3：**主启动类

~~~java
@SpringBootApplication
@EnableDiscoveryClient
public class OrderZKMain80 {
    public static void main(String[] args) {
        SpringApplication.run(OrderZKMain80.class, args);
    }
}
~~~

**步骤4：**业务类

~~~java
@RestController
@Slf4j
public class OrderZKController {
    public static final String INVOKE_URL = "http://localhost:8004";

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping(value = "/consumer/payment/zk")
    public String paymentInfo() {
        String result = restTemplate.getForObject(INVOKE_URL + "/payment/zk", String.class);
        return result;
    }
}
~~~

**步骤5：**测试

1. zookeeper客户端的/services路径下有了服务消费者
2. 访问 http://localhost/consumer/payment/zk 正常输出



## Consul服务注册中心

[官网地址](https://www.consul.io/docs/intro)

- Consul 是一套开源的分布式服务发现和配置管理系统，由HashiCorp 公司用Go语言开发。
- 提供了微服务系统中的服务治理、配置中心、控制总线等功能。这些功能中的每一个都可以根据需要单独使用，也可以一起使用以构建全方位的服务网格，总之Consul提供了一种完整的服务网格解决方案。
- 服务发现：提供HTTP和DNS两种发现方式
- 健康检测：支持多种协议，HTTP、TCP、Docker、Shell脚本定制化
- KV存储：Key、Value的存储方式
- 多数据中心：Consul支持多数据中心
- 可视化Web界面

**中文教程：**https://www.springcloud.cc/spring-cloud-consul.html

> 放弃Consul，HashiCorp官宣，禁止其旗下Consul等软件在国内使用



