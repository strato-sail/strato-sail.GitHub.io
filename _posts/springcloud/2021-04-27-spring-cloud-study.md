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
  - Ribbon
---

[TOC]



# 简介

**SpringCloud**（[官网地址](https://spring.io/projects/spring-cloud)）是分布式微服务架构的一站式解决方案，是多种微服务架构落地技术的集合体，俗称微服务全家桶。

**springCloud和springBoot版本适配关系：**

![版本适配关系](\img\in-post\springcloud\springcloud-01.png)

详情见：[spring cloud2020.0.2 + spring boot2.4.2官方文档](https://docs.spring.io/spring-cloud/docs/current/reference/html/)



# 服务注册中心



## Eureka 服务注册与发现(停更)

<font color=#0044FF size=5>**1）Eureka 概念**</font>

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



<font color=#0044FF size=5>**2）单机版的 eureka**</font>

<font color=#000000 size=4>**步骤1：**</font>引入pom依赖

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

<font color=#000000 size=4>**步骤2：**</font>配置文件

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

<font color=#000000 size=4>**步骤3：**</font>主启动类

~~~java
@SpringBootApplication
@EnableEurekaServer   //启动eureka服务端
public class EurekaMain7001 {
    public static void main(String[] args) {
        SpringApplication.run(EurekaMain7001.class, args);
    }
}
~~~

<font color=#000000 size=4>**步骤4：**</font>可以启动当前项目，并访问 http://localhost:7001 查看web端的注册中心。

<font color=#000000 size=4>**步骤5：**</font>将服务提供者和消费者注册到eureka

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



<font color=#0044FF size=5>**3）集群版的 eureka**</font>

为了高可用使用集群版，实现负载均衡+故障容错。

- 需要注册中心集群化和服务提供者集群化。
- 注册中心**互相注册，相互守望**

<font color=#000000 size=4>**步骤1：**</font>将不同的server名字更改

在单机模拟的时候，改本机host文件

![](\img\in-post\springcloud\springcloud-03.png)

<font color=#000000 size=4>**步骤2：**</font>改 7001 的配置文件

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

<font color=#000000 size=4>**步骤3：**</font>启动 server7001 和 server7002，成功启动后 http://eureka7001.com:7001/ 有复制冗余了

![](\img\in-post\springcloud\springcloud-04.png)

<font color=#000000 size=4>**步骤4：**</font>将服务提供者的配置更改

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

<font color=#000000 size=4>**步骤5：**</font>测试先启动EurekaServer7001/7002，然后启动服务提供者8001，然后启动服务消费者80，访问 http://localhost/consumer/payment/get/31 。

<font color=#000000 size=4>**步骤6：**</font>服务提供者集群化配置，增加可用性

模仿 `cloud-provider-payment8001` 这个项目，创建`payment8002`

<font color=#000000 size=4>**步骤7：**</font>改服务调用者的 `Controler`，将访问的 `url` 改为注册中心注册的服务名：

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

<font color=#000000 size=4>**步骤8：**</font>可以将注册中心的服务提供者名字改了，修改yml配置文件：

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

<font color=#000000 size=4>**步骤9：DiscoveryClient **</font>对于注册进`eureka`里面的微服务，可以通过服务发现来获得该服务的信息：

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

<font color=#000000 size=4>**步骤10：**</font>Eureka 自我保护模式

- 保护模式主要用于一组客户端和Eureka Server之间存在网络分区场景下的保护。
- 一旦进入保护模式，Eureka Server 就会尝试保护其服务注册表中的信息，不再删除服务注册表中的数据，也就是**不会注销任何微服务**。
- 某时刻某一个微服务不可用了，Eureka不会立即清理，依旧会对该微服务的信息进行保存。
- 属于CAP里面的AP分支
- 为了防止 EurekaClient 可以正常运行，但是与 EurekaServer 网络不通情况下，EurekaServer 不会立刻将 EurekaClient 服务剔除
- 默认情况下，如果EurekaServer在一定时间内没有接收到某个微服务实例的心跳，EurekaServer将会注销该实例（默认90秒)。但是当网络分区故障发生(延时、卡顿、拥挤)时，微服务与EurekaSerser之间无法正常通信，以上行为可能变得非常危险了——因为微服务本身其实是健康的，**此时本不应该注销这个微服务。**Eureka通过“自我保护模式”来解决这个问题——当EurekaServer节点在短时间内丢失过多客户端时（可能发生了网络分区故障)，那么这个节点就会进入自我保护模式。

<font color=#000000 size=4>**禁止自我保护的方式：**</font>

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

<font color=#0044FF size=5>**1）服务提供者**</font>

<font color=#000000 size=4>**步骤1：**</font>POM依赖

~~~xml
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-zookeeper-discovery</artifactId>
</dependency>
~~~

<font color=#000000 size=4>**步骤2：**</font>YML配置文件

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

<font color=#000000 size=4>**步骤3：**</font>主启动类

~~~java
@SpringBootApplication
@EnableDiscoveryClient  //用于向使用consul或者zookeeper作为注册中心时注册服务
public class PaymentMain8004 {
    public static void main(String[] args) {
        SpringApplication.run(PaymentMain8004.class, args);
    }
}
~~~

<font color=#000000 size=4>**步骤4：**</font>controller

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

<font color=#000000 size=4>**步骤5：**</font>测试，启动8004注册进zookeeper

1. zookeeper根节点下出现了services新节点，services节点下有个cloud-provider-payment节点，就是步骤2中自定义的服务名。**zookeeper的服务节点是 -e 临时的**

   ~~~shell
   [zk: localhost:2181(CONNECTED) 12] get /services/cloud-provider-payment/0f0a2cbc-4ba3-4ffa-9fcc-4daa8701a320 
   {"name":"cloud-provider-payment","id":"0f0a2cbc-4ba3-4ffa-9fcc-4daa8701a320","address":"localhost","port":8004,"sslPort":null,"payload":{"@class":"org.springframework.cloud.zookeeper.discovery.ZookeeperInstance","id":"application-1","name":"cloud-provider-payment","metadata":{}},"registrationTimeUTC":1620286926493,"serviceType":"DYNAMIC","uriSpec":{"parts":[{"value":"scheme","variable":true},{"value":"://","variable":false},{"value":"address","variable":true},{"value":":","variable":false},{"value":"port","variable":true}]}}
   ~~~

2. 进入链接 http://localhost:8004/payment/zk 测试。



<font color=#0044FF size=5>**2）服务消费者**</font>

<font color=#000000 size=4>**步骤1：**</font>POM文件

~~~xml
<!--zookeeper-->
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-zookeeper-discovery</artifactId>
</dependency>
~~~

<font color=#000000 size=4>**步骤2：**</font>YAML配置文件

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

<font color=#000000 size=4>**步骤3：**</font>主启动类

~~~java
@SpringBootApplication
@EnableDiscoveryClient
public class OrderZKMain80 {
    public static void main(String[] args) {
        SpringApplication.run(OrderZKMain80.class, args);
    }
}
~~~

<font color=#000000 size=4>**步骤4：**</font>业务类

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

<font color=#000000 size=4>**步骤5：**</font>测试

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



# 负载均衡服务调用



## Ribbon负载均衡

<font color=#0044FF size=5>**1）概念**</font>

- Ribbon是Netflix发布的开源项目，主要功能是提供客户端的软件负载均衡算法和服务调用。
- Ribbon客户端组件提供一系列完善的配置项如连接超时，重试等。简单的说，就是在配置文件中列出Load Balancer (简称LB)后面所有的机器，Ribbon会自动的帮助你基于某种规则(如简单轮询，随机连接等）去连接这些机器。很容易使用Ribbon实现自定义的负载均衡算法。
- Ribbon集成于消费方进程
- Ribbon是负载均衡＋RestTemplate调用



<font color=#000000 size=4>**Ribboh本地负载均衡客户端  VS  Nginx服务端负载均衡区别**</font>

> - 集中式LB（Load Balanced）：Nginx是服务器负载均衡，客户端所有请求都会交给nginx，然后由nginx实现转发请求。即负载均衡是由服务端实现的
>- 进程内LB：Ribbon本地负载均衡，在调用微服务接口时候，会在注册中心上获取注册信息服务列表之后缓存到JVM本地从而在本地实现RPC远程服务调用技术。（消费方从服务注册中心获知有哪些地址可用，然后自己再从这些地址中选择出一个合适的服务器调用）



<font color=#000000 size=4>**步骤1：**</font>**架构图：**

Ribbon 在工作的时候分两步：

1. 先选择EurekaServer，它优先选择在同一个区域内负载较少的server
2. 再根据用户指定的策略，在从server取到的服务注册列表中选择一个地址
3. Ribbon 提供了多种策略：轮询，随机和根据响应时间加权。

![](F:\strato-sail\strato-sail.GitHub.io\img\in-post\springcloud\springcloud-05.png)



<font color=#0044FF size=5>**2）使用**</font>

Ribbon 核心组件 IRule：根据特定算法中从服务列表中选取一个要访问的服务。



<font color=#000000 size=4>**步骤1：**</font>POM文件

引入`spring-cloud-starter-netflix-eureka-client`依赖就自动引入了`spring-cloud-starter-netflix-ribbon`依赖。



<font color=#000000 size=4>**步骤2：**</font>IRule配置类

> 警告!!
>
> 这个自定义配置类不能放在@ComponentScan（@SpringBootApplication注解继承的）所扫描的当前包吓以及子包下，
> 否则我们自定义的这个配置类就会被所有的Ribbon客户端所共享，达不到特殊化定制的目的了。

![](F:\strato-sail\strato-sail.GitHub.io\img\in-post\springcloud\springcloud-06.png)



<font color=#000000 size=4>**负载均衡算法：**</font>

1. com.netflix.loadbalancer.RoundRobinRule：轮询
2. com.netflix.loadbalancer.RandomRule：随机
3. com.netflix.loadbalancer.RetryRule：先按照RoundRobinRule的策略获取服务，如果获取服务失败则在指定时间内会进行重试
4. WeightedResponseTimeRule ：对RoundRobinRule的扩展，响应速度越快的实例选择权重越大，越容易被选择
5. BestAvailableRule ：会先过滤掉由于多次访问故障而处于断路器跳闸状态的服务，然后选择一个并发量最小的服务
6. AvailabilityFilteringRule ：先过滤掉故障实例，再选择并发较小的实例
7. ZoneAvoidanceRule：默认规则，复合判断server所在区域的性能和server的可用性选择服务器



<font color=#000000 size=4>**步骤3：**</font>主启动类上添加`@RibbonClient`

~~~java
@SpringBootApplication
@EnableEurekaClient
@RibbonClient(name = "CLOUD-PAYMENT-SERVICE",configuration = MySelfRule.class)
public class OrderMain80 {

    public static void main(String[] args) {
        SpringApplication.run(OrderMain80.class, args);
    }
}
~~~

测试 http://localhost/consumer/payment/get/31 ，发现已经变成了随机。



<font color=#0044FF size=5>**3）Ribbon负载均衡算法原理**</font>

负载均衡算法: 

- rest接口第几次请求数 % 服务器集群总数量 = 实际调用服务器位置下标
- 每次服务重启动后rest接口计数从1开始.



<font color=#000000 size=4>**轮询算法**</font>

~~~java
//外面包装一层CAS自旋锁操作用于多线程
//传入的modulo为服务器集群数量
incrementAndGetModulo( int modulo) {
    List<ServiceInstance> instances = discoveryClient.getInstances("Service-Name");
    int current = nextServerCyclicCounter.get();
	int next = (current + 1) % modulo;
}
~~~



## OpenFeign服务接口调用

[官网地址](https://github.com/spring-cloud/spring-cloud-openfeign)

<font color=#0044FF size=5>**1）概念**</font>

- Feign 是一个声明式的 web 服务客户端，让编写 web 服务客户端变得非常容易，只需创建一个接口并在接口上添加注解即可。
- 它的使用方法是定义一个服务接口然后在上面添加注解。Feign 也支持可拔插式的编码器和解码器。
- 由 Feign 来帮助我们定义和实现依赖服务接口的定义。在 Feign 的实现下我们只需创建一个接口并使用注解的方式来配置它(以前是 Dao 接口上面标注 Mapper 注解,现在是一个微服务接口上面标注一个 Feign 注解即可)，即可完成对服务提供方的接口绑定，简化了使用 Spring cloud Ribbon 时，自动封装服务调用客户端的开发量。
- Feign 集成了 Ribbon：利用 Ribbon 维护了 Payment 的服务列表信息，并且通过轮询实现了客户端的负载均衡。而与 Ribbon 不同的是，通过 feign 只需要定义服务绑定接口且以声明式的方法，优雅而简单的实现了服务调用。
- OpenFeign 是 Spring Cloud 在 Feign 的基础上支持了 SpringMVC 的注解，如` @RequesMapping `等等。OpenFeign 的 `@FeignClient` 可以解析 SpringMvc 的 `@RequestMapping` 注解下的接口，并通过动态代理的方式产生实现类，实现类中做负载均衡并调用其他服务。
- Feign 在消费端使用。
- Feign自带负载均衡配置项（集成了Ribbon）。



<font color=#0044FF size=5>**2）使用**</font>

微服务调用接口 + @FeignClient

<font color=#000000>  步骤如下：</font>

1. 写一个接口，此接口使用`@FeignClient(name = "CLOUD-PAYMENT-SERVICE")`标记，根据配置文件自动连接到注册中心并寻找对应服务名的服务。
2. 在controller层自动注入`@Autowired`接口类，实现类会自动远程调用注册中心的服务。
3. 在主启动类上标记`@EnableFeignClients `启动Feign功能。



<font color=#000000 size=4>**步骤1：**</font>POM文件添加依赖

~~~xml
<!-- openfeign自动整合了ribbon -->
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-openfeign</artifactId>
	<version>2.2.1.RELEASE</version>
</dependency>
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
	<version>2.2.1.RELEASE</version>
</dependency>
~~~



<font color=#000000 size=4>**步骤2：**</font>配置文件

~~~yaml
server:
  port: 80
eureka:
  client:
    register-with-eureka: false
    service-url:
      defaultZone: http://eureka7001.com:7001/eureka,http://eureka7002.com:7002/eureka
~~~



<font color=#000000 size=4>**步骤3：**</font>主启动类

~~~java
@SpringBootApplication
@EnableFeignClients   //启动Feign功能
public class OrderFeignMain80 {
    public static void main(String[] args) {
        SpringApplication.run(OrderFeignMain80.class, args);
    }
}
~~~



<font color=#000000 size=4>**步骤4：**</font>业务类

- 业务逻辑接口+`@FeignClient`配置调用provider服务

- 新建PaymentFeignService接口并新增注解`@FeignClient`

  ~~~java
  @Component
  @FeignClient(name = "CLOUD-PAYMENT-SERVICE")  //找Eureka上对应服务名的服务
  public interface PaymentFeignService {
  
      @GetMapping(value = "/payment/get/{id}")
      public CommonResult<Payment> get(@PathVariable("id") Long id);
  
  }
  ~~~

- 控制层Controller

  ~~~java
  @RestController
  @Slf4j
  public class OrderFeignController {
      @Autowired
      private PaymentFeignService paymentFeignService;
  
      @GetMapping("/consumer/payment/get/{id}")
      public CommonResult<Payment> getPaymentById(@PathVariable("id") Long id){
          return paymentFeignService.get(id);
      }
  }
  ~~~



<font color=#000000 size=4>**步骤5：**</font>测试

访问 http://localhost/consumer/payment/get/31。

Feign自带负载均衡配置项。



<font color=#0044FF size=5>**3）超时控制**</font>

OpenFeign 默认等待一秒钟，超时直接报错。

yml 配置文件中设置客户端超时时间：

~~~yaml
server:
  port: 80
eureka:
  client:
    register-with-eureka: false
    service-url:
      defaultZone: http://eureka7001.com:7001/eureka,http://eureka7002.com:7002/eureka
#没置feign客端超时时同(openFeign默认支持ribbon)
ribbon:
#指的是建立连接后从服务器读取到可用资源所用的时间
  ReadTimeout: 5000
#指的是建立连接所用的时间,适用于网络状况正常的情况下,两端连接所用的时间
  ConnectTimeout: 5000
~~~



<font color=#0044FF size=5>**4）OpenFeign日志打印功能**</font>

