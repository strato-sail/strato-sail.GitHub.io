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
  - OpenFeign
  - Hystrix
  - Gateway
  - Bus
  - Cloud Stream
  - sentinel
---





# 简介



**SpringCloud**（[官网地址](https://spring.io/projects/spring-cloud)）是分布式微服务架构的一站式解决方案，是多种微服务架构落地技术的集合体，俗称微服务全家桶。



**内容：**

![](\img\in-post\springcloud\springcloud-00.png)



**springCloud和springBoot版本适配关系：**

![版本适配关系](\img\in-post\springcloud\springcloud-01.png)

详情见：[spring cloud2020.0.2 + spring boot2.4.2官方文档](https://docs.spring.io/spring-cloud/docs/current/reference/html/)



# 1 服务注册中心



## Eureka 服务注册与发现(停更)

<span style="color:#0044FF;font-size:15.0pt;font-weight:bold">1）Eureka 概念</span>

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



<span style="color:#0044FF;font-size:15.0pt;font-weight:bold">2）单机版的 eureka</span>

<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤1：</span>引入pom依赖

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

<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤2：</span>配置文件

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

<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤3：</span>主启动类

~~~java
@SpringBootApplication
@EnableEurekaServer   //启动eureka服务端
public class EurekaMain7001 {
    public static void main(String[] args) {
        SpringApplication.run(EurekaMain7001.class, args);
    }
}
~~~

<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤4：</span>可以启动当前项目，并访问 http://localhost:7001 查看web端的注册中心。

<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤5：</span>将服务提供者和消费者注册到eureka

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



<span style="color:#0044FF;font-size:15.0pt;font-weight:bold">3）集群版的 eureka</span>

为了高可用使用集群版，实现负载均衡+故障容错。

- 需要注册中心集群化和服务提供者集群化。
- 注册中心**互相注册，相互守望**

<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤1：</span>将不同的server名字更改

在单机模拟的时候，改本机host文件

![](\img\in-post\springcloud\springcloud-03.png)

<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤2：</span>改 7001 的配置文件

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

<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤3：</span>启动 server7001 和 server7002，成功启动后 http://eureka7001.com:7001/ 有复制冗余了

![](\img\in-post\springcloud\springcloud-04.png)

<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤4：</span>将服务提供者的配置更改

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

<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤5：</span>测试先启动EurekaServer7001/7002，然后启动服务提供者8001，然后启动服务消费者80，访问 http://localhost/consumer/payment/get/31 。

<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤6：</span>服务提供者集群化配置，增加可用性

模仿 `cloud-provider-payment8001` 这个项目，创建`payment8002`

<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤7：</span>改服务调用者的 `Controler`，将访问的 `url` 改为注册中心注册的服务名：

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

<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤8：</span>可以将注册中心的服务提供者名字改了，修改yml配置文件：

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

<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤9：</span>对于注册进`eureka`里面的微服务，可以通过服务发现来获得该服务的信息：

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

<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤10：</span>Eureka 自我保护模式

- 保护模式主要用于一组客户端和Eureka Server之间存在网络分区场景下的保护。
- 一旦进入保护模式，Eureka Server 就会尝试保护其服务注册表中的信息，不再删除服务注册表中的数据，也就是**不会注销任何微服务**。
- 某时刻某一个微服务不可用了，Eureka不会立即清理，依旧会对该微服务的信息进行保存。
- 属于CAP里面的AP分支
- 为了防止 EurekaClient 可以正常运行，但是与 EurekaServer 网络不通情况下，EurekaServer 不会立刻将 EurekaClient 服务剔除
- 默认情况下，如果EurekaServer在一定时间内没有接收到某个微服务实例的心跳，EurekaServer将会注销该实例（默认90秒)。但是当网络分区故障发生(延时、卡顿、拥挤)时，微服务与EurekaSerser之间无法正常通信，以上行为可能变得非常危险了——因为微服务本身其实是健康的，**此时本不应该注销这个微服务。**Eureka通过“自我保护模式”来解决这个问题——当EurekaServer节点在短时间内丢失过多客户端时（可能发生了网络分区故障)，那么这个节点就会进入自我保护模式。

<span style="color:#000000;font-size:14.0pt;font-weight:bold">禁止自我保护的方式：</span>

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

<span style="color:#0044FF;font-size:15.0pt;font-weight:bold">1）服务提供者</span>

<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤1：</span>POM依赖

~~~xml
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-zookeeper-discovery</artifactId>
</dependency>
~~~

<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤2：</span>YML配置文件

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

<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤3：</span>主启动类

~~~java
@SpringBootApplication
@EnableDiscoveryClient  //用于向使用consul或者zookeeper作为注册中心时注册服务
public class PaymentMain8004 {
    public static void main(String[] args) {
        SpringApplication.run(PaymentMain8004.class, args);
    }
}
~~~

<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤4：</span>controller

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

<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤5：</span>测试，启动8004注册进zookeeper

1. zookeeper根节点下出现了services新节点，services节点下有个cloud-provider-payment节点，就是步骤2中自定义的服务名。**zookeeper的服务节点是 -e 临时的**

   ~~~shell
   [zk: localhost:2181(CONNECTED) 12] get /services/cloud-provider-payment/0f0a2cbc-4ba3-4ffa-9fcc-4daa8701a320 
   {"name":"cloud-provider-payment","id":"0f0a2cbc-4ba3-4ffa-9fcc-4daa8701a320","address":"localhost","port":8004,"sslPort":null,"payload":{"@class":"org.springframework.cloud.zookeeper.discovery.ZookeeperInstance","id":"application-1","name":"cloud-provider-payment","metadata":{}},"registrationTimeUTC":1620286926493,"serviceType":"DYNAMIC","uriSpec":{"parts":[{"value":"scheme","variable":true},{"value":"://","variable":false},{"value":"address","variable":true},{"value":":","variable":false},{"value":"port","variable":true}]}}
   ~~~

2. 进入链接 http://localhost:8004/payment/zk 测试。



<span style="color:#0044FF;font-size:15.0pt;font-weight:bold">2）服务消费者</span>

<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤1：</span>POM文件

~~~xml
<!--zookeeper-->
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-zookeeper-discovery</artifactId>
</dependency>
~~~

<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤2：</span></font>YAML配置文件

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

<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤3：</span>主启动类

~~~java
@SpringBootApplication
@EnableDiscoveryClient
public class OrderZKMain80 {
    public static void main(String[] args) {
        SpringApplication.run(OrderZKMain80.class, args);
    }
}
~~~

<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤4：</span>业务类

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

<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤5：</span>测试

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



# 2 负载均衡服务调用



## Ribbon负载均衡

<span style="color:#0044FF;font-size:15.0pt;font-weight:bold">1）概念</span>

- Ribbon是Netflix发布的开源项目，主要功能是提供客户端的软件负载均衡算法和服务调用。
- Ribbon客户端组件提供一系列完善的配置项如连接超时，重试等。简单的说，就是在配置文件中列出Load Balancer (简称LB)后面所有的机器，Ribbon会自动的帮助你基于某种规则(如简单轮询，随机连接等）去连接这些机器。很容易使用Ribbon实现自定义的负载均衡算法。
- Ribbon集成于消费方进程
- Ribbon是负载均衡＋RestTemplate调用



<span style="color:#000000;font-size:14.0pt;font-weight:bold">Ribboh本地负载均衡客户端  VS  Nginx服务端负载均衡区别</span>

> - 集中式LB（Load Balanced）：Nginx是服务器负载均衡，客户端所有请求都会交给nginx，然后由nginx实现转发请求。即负载均衡是由服务端实现的
>- 进程内LB：Ribbon本地负载均衡，在调用微服务接口时候，会在注册中心上获取注册信息服务列表之后缓存到JVM本地从而在本地实现RPC远程服务调用技术。（消费方从服务注册中心获知有哪些地址可用，然后自己再从这些地址中选择出一个合适的服务器调用）



<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤1：</span>**架构图：**

Ribbon 在工作的时候分两步：

1. 先选择EurekaServer，它优先选择在同一个区域内负载较少的server
2. 再根据用户指定的策略，在从server取到的服务注册列表中选择一个地址
3. Ribbon 提供了多种策略：轮询，随机和根据响应时间加权。

![](\img\in-post\springcloud\springcloud-05.png)



<span style="color:#0044FF;font-size:15.0pt;font-weight:bold">2）使用</span>

Ribbon 核心组件 IRule：根据特定算法中从服务列表中选取一个要访问的服务。



<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤1：</span>POM文件

引入`spring-cloud-starter-netflix-eureka-client`依赖就自动引入了`spring-cloud-starter-netflix-ribbon`依赖。



<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤2：</span>IRule配置类

> 警告!!
>
> 这个自定义配置类不能放在@ComponentScan（@SpringBootApplication注解继承的）所扫描的当前包吓以及子包下，
> 否则我们自定义的这个配置类就会被所有的Ribbon客户端所共享，达不到特殊化定制的目的了。

![](\img\in-post\springcloud\springcloud-06.png)



<span style="color:#000000;font-size:14.0pt;font-weight:bold">负载均衡算法：</span>

1. com.netflix.loadbalancer.RoundRobinRule：轮询
2. com.netflix.loadbalancer.RandomRule：随机
3. com.netflix.loadbalancer.RetryRule：先按照RoundRobinRule的策略获取服务，如果获取服务失败则在指定时间内会进行重试
4. WeightedResponseTimeRule ：对RoundRobinRule的扩展，响应速度越快的实例选择权重越大，越容易被选择
5. BestAvailableRule ：会先过滤掉由于多次访问故障而处于断路器跳闸状态的服务，然后选择一个并发量最小的服务
6. AvailabilityFilteringRule ：先过滤掉故障实例，再选择并发较小的实例
7. ZoneAvoidanceRule：默认规则，复合判断server所在区域的性能和server的可用性选择服务器



<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤3：</span>主启动类上添加`@RibbonClient`

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



<span style="color:#0044FF;font-size:15.0pt;font-weight:bold">3）Ribbon负载均衡算法原理</span>

负载均衡算法: 

- rest接口第几次请求数 % 服务器集群总数量 = 实际调用服务器位置下标
- 每次服务重启动后rest接口计数从1开始.



<span style="color:#000000;font-size:14.0pt;font-weight:bold">轮询算法</span>

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

<span style="color:#0044FF;font-size:15.0pt;font-weight:bold">1）概念</span>

- Feign 是一个声明式的 web 服务客户端，让编写 web 服务客户端变得非常容易，只需创建一个接口并在接口上添加注解即可。
- 它的使用方法是定义一个服务接口然后在上面添加注解。Feign 也支持可拔插式的编码器和解码器。
- 由 Feign 来帮助我们定义和实现依赖服务接口的定义。在 Feign 的实现下我们只需创建一个接口并使用注解的方式来配置它(以前是 Dao 接口上面标注 Mapper 注解,现在是一个微服务接口上面标注一个 Feign 注解即可)，即可完成对服务提供方的接口绑定，简化了使用 Spring cloud Ribbon 时，自动封装服务调用客户端的开发量。
- Feign 集成了 Ribbon：利用 Ribbon 维护了 Payment 的服务列表信息，并且通过轮询实现了客户端的负载均衡。而与 Ribbon 不同的是，通过 feign 只需要定义服务绑定接口且以声明式的方法，优雅而简单的实现了服务调用。
- OpenFeign 是 Spring Cloud 在 Feign 的基础上支持了 SpringMVC 的注解，如` @RequesMapping `等等。OpenFeign 的 `@FeignClient` 可以解析 SpringMvc 的 `@RequestMapping` 注解下的接口，并通过动态代理的方式产生实现类，实现类中做负载均衡并调用其他服务。
- Feign 在消费端使用。
- Feign自带负载均衡配置项（集成了Ribbon）。



<span style="color:#0044FF;font-size:15.0pt;font-weight:bold">2）使用</span>

微服务调用接口 + @FeignClient

<font color=#000000>  步骤如下：</font>

1. 写一个接口，此接口使用`@FeignClient(name = "CLOUD-PAYMENT-SERVICE")`标记，根据配置文件自动连接到注册中心并寻找对应服务名的服务。
2. 在controller层自动注入`@Autowired`接口类，实现类会自动远程调用注册中心的服务。
3. 在主启动类上标记`@EnableFeignClients `启动Feign功能。



<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤1：</span>POM文件添加依赖

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



<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤2：</span>配置文件

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



<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤3：</span>主启动类

~~~java
@SpringBootApplication
@EnableFeignClients   //启动Feign功能
public class OrderFeignMain80 {
    public static void main(String[] args) {
        SpringApplication.run(OrderFeignMain80.class, args);
    }
}
~~~



<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤4：</span>业务类

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



<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤5：</span>测试

访问 http://localhost/consumer/payment/get/31。

Feign自带负载均衡配置项。



<span style="color:#0044FF;font-size:15.0pt;font-weight:bold">3）超时控制</span>

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



<span style="color:#0044FF;font-size:15.0pt;font-weight:bold">4）OpenFeign日志打印功能</span>

<span style="color:#000000;font-size:14.0pt;font-weight:bold">日志级别：</span>

- NONE：默认的，不显示任何日志;
- BASIC：仅记录请求方法、URL、响应状态码及执行时间;
- HEADERS：除了BASIC中定义的信息之外，还有请求和响应的头信息;
- FULL：除了HEADERS中定义的信息之外，还有请求和响应的正文及元数据。

<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤1：</span>建一个配置bean

~~~java
@Configuration
public class FeignConfig {
    @Bean
    Logger.Level feignLoggerLevel(){
        return Logger.Level.FULL;
    }
}
~~~

<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤2：</span>Yaml 文件里需要开启日志的 Feign 客户端

~~~yaml
logging:
  level: 
    # feign 日志以什么级别监控哪个接口
    com.wcy.springcloud.service.PaymentFeignService: debug
~~~

<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤3：</span>后台日志查看

~~~shell
2021-05-08 10:20:22.412 DEBUG 15448 --- [p-nio-80-exec-1] c.w.s.service.PaymentFeignService        : [PaymentFeignService#get] <--- HTTP/1.1 200 (450ms)
2021-05-08 10:20:22.412 DEBUG 15448 --- [p-nio-80-exec-1] c.w.s.service.PaymentFeignService        : [PaymentFeignService#get] connection: keep-alive
2021-05-08 10:20:22.412 DEBUG 15448 --- [p-nio-80-exec-1] c.w.s.service.PaymentFeignService        : [PaymentFeignService#get] content-type: application/json
2021-05-08 10:20:22.412 DEBUG 15448 --- [p-nio-80-exec-1] c.w.s.service.PaymentFeignService        : [PaymentFeignService#get] date: Sat, 08 May 2021 02:20:22 GMT
2021-05-08 10:20:22.412 DEBUG 15448 --- [p-nio-80-exec-1] c.w.s.service.PaymentFeignService        : [PaymentFeignService#get] keep-alive: timeout=60
2021-05-08 10:20:22.412 DEBUG 15448 --- [p-nio-80-exec-1] c.w.s.service.PaymentFeignService        : [PaymentFeignService#get] transfer-encoding: chunked
2021-05-08 10:20:22.412 DEBUG 15448 --- [p-nio-80-exec-1] c.w.s.service.PaymentFeignService        : [PaymentFeignService#get] 
2021-05-08 10:20:22.415 DEBUG 15448 --- [p-nio-80-exec-1] c.w.s.service.PaymentFeignService        : [PaymentFeignService#get] {"code":200,"message":"查询数据库成功,serverPort:8001","data":{"id":31,"serial":"wcy"}}
2021-05-08 10:20:22.415 DEBUG 15448 --- [p-nio-80-exec-1] c.w.s.service.PaymentFeignService        : [PaymentFeignService#get] <--- END HTTP (94-byte body)
2021-05-08 10:20:23.165  INFO 15448 --- [erListUpdater-0] c.netflix.config.ChainedDynamicProperty  : Flipping property: CLOUD-PAYMENT-SERVICE.ribbon.ActiveConnectionsLimit to use NEXT property: niws.loadbalancer.availabilityFilteringRule.activeConnectionsLimit = 2147483647
~~~



# 3 服务限流



## Hystrix 服务降级、熔断



[官网学习使用地址](https://github.com/Netflix/Hystrix/wiki/How-To-Use)、[官网原理地址](https://github.com/Netflix/Hystrix/wiki/How-it-Works)、[源码](https://github.com/Netflix/Hystrix)



Hystrix 可以在服务端也可以在客户端，一般用于**客户端**。



<span style="color:#000000;font-size:14.0pt;font-weight:bold">分布式系统面临的问题：</span>分布式关系中的一个服务调用失败

**服务雪崩：**多个微服务之间调用的时候，假设微服务A调用微服务B和微服务C，微服务B和微服务C又调用其它的微服务，这就是所谓的“<span style="color:red">扇出</span>”。如果扇出的链路上某个微服务的调用响应时间过长或者不可用，对微服务A的调用就会占用越来越多的系统资源，进而引起系统崩溃，所谓的“雪崩效应”.



<span style="color:#0044FF;font-size:15.0pt;font-weight:bold">1）概念</span>

<span style="color:#000000;font-size:14.0pt;font-weight:bold">是什么：</span>

- Hystrix是一个用于处理分布式系统的<span style="color:red">延迟和容错</span>的开源库，在分布式系统里，许多依赖不可避免的会调用失败，比如超时、异常等，<span style="color:red">Hystrix能够保证在一个依赖出问题的情况下，不会导致整体服务失败，避免级联故障，以提高分布式系统的弹性</span>。
- "断路器”本身是一种开关装置，当某个服务单元发生故障之后，通过断路器的故障监控（类似熔断保险丝)，<span style="color:red">向调用方返回一个符合预期的、可处理的备选响应(FallBack)，而不是长时间的等待或者抛出调用方无法处理的异常</span>，这样就保证了服务调用方的线程不会被长时间、不必要地占用，从而避免了故障在分布式系统中的蔓延，乃至雪崩。



<span style="color:#000000;font-size:14.0pt;font-weight:bold">能干什么：</span>

- **服务降级 <span style="Times New Roman">fallback</span>：**对方系统不可用了，返回备选响应。程序运行异常（下标越界），超时，服务熔断触发服务降级，线程池满。
- **服务熔断 break：**（保险丝）达到最大服务访问后，直接拒绝访问，然后调用服务降级的方法友好返回。
- **服务限流 flowlimit：**秒杀高并发操作。
- **接近实时的监控：**



<span style="color:#000000;font-size:14.0pt;font-weight:bold">工作流程：</span>

![](\img\in-post\springcloud\springcloud-08.png)

解释上图：

1. 两个注解都可以声明方法熔断
2. 两个注解二选一
3. 在缓存中查找，如果缓存中有则直接返回
4. 熔断器是否为打开状态，如果为打开状态则直接转到第8步 fallback
5. 检查是否有足够的资源执行，信号量、线程池等，如果满了转到第8步 fallback
6. 进行构造方法和run()方法，根据第一步的选择
7. 会把请求正常和失败的数据告诉断路器，断路器维护一个计数器来保存
8. 进行服务降级，调用 fallback 方法，看 fallback 是否成功（第4，5步）
9. 将结果返回



<span style="color:#0044FF;font-size:15.0pt;font-weight:bold">2）案例使用</span>

<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤1：</span>建立带Hystrix的服务提供者模块

**pom 依赖：**

~~~xml
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
	<version>1.4.4.RELEASE</version>
</dependency>
~~~

**yaml 配置文件：**

~~~yaml
server:
  port: 8001
spring:
  application:
    name: cloud-provider-hystrix-payment
eureka:
  client:
    service-url:
      defaultZone: http://eureka7001.com:7001/eureka
    register-with-eureka: true
    fetch-registry: true
~~~

**主启动类：**

~~~java
@SpringBootApplication
@EnableEurekaClient
public class PaymentHystrixMain8001 {
    public static void main(String[] args) {
        SpringApplication.run(PaymentHystrixMain8001.class, args);
    }
}
~~~

**业务类：**

~~~java
@Service
public class PaymentService {
    public String paymentInfo_OK(Integer id) {
        return "线程池：" + Thread.currentThread().getName() + "paymentInfo_OK，id：" + id;
    }

    public String paymentInfo_Timeout(Integer id) {
        int timeNum = 3;
        try {  //故意超时
            TimeUnit.SECONDS.sleep(timeNum);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "线程池：" + Thread.currentThread().getName() + "paymentInfo_Timeout，id：" + id + "\t耗时3秒钟，耗时（秒）：" + timeNum;
    }
}
~~~

~~~java
@RestController
@Slf4j
public class PaymentController {
    @Autowired
    private PaymentService paymentService;

    @Value("${server.port}")
    private String serverPort;

    @GetMapping("/payment/hystrix/ok/{id}")
    public String paymentInfo_OK(@PathVariable("id") Integer id) {
        String result = paymentService.paymentInfo_OK(id);
        log.info("result:" + result);
        return result;
    }

    @GetMapping("/payment/hystrix/timeout/{id}")
    public String paymentInfo_Timeout(@PathVariable("id") Integer id) {
        String result = paymentService.paymentInfo_Timeout(id);
        log.info("result:" + result);
        return result;
    }
}
~~~

<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤2：</span>测试

http://localhost:8001/payment/hystrix/ok/31

http://localhost:8001/payment/hystrix/timeout/31

均正常访问。



> 使用 OpenFeign 创建服务消费者模块，过程不用多说了细节就是服务接口上的`@FeignClient(value = "CLOUD-PROVIDER-HYSTRIX-PAYMENT")`注解，和主启动类上的`@EnableFeignClients`注解，配置文件`eureka.client.service-url = defaultZone: http://eureka7001.com:7001/eureka/` 且加上ribbon的超时设置



**现象：**用 <span style="color:#FFA000;font-size:12.0pt;font-weight:bold">Apache JMeter</span> 测试，200个线程，周期1秒钟，循环100次，去请求 http://localhost:80/consumer/payment/hystrix/timeout/31 ，两个服务接口都卡了。

**原因：**同一层次的其他接口服务困死，tomcat 线程里面的工作线程已经被挤占完毕。



<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤3：</span>解决步骤2 出现的问题

- 服务超时、宕机了，服务调用者不能一直卡死等待，得有服务降级
- 调用者自己有自我要求：自己等待时间小于服务提供者真实处理时间，自己处理降级



<span style="color:#0044FF;font-size:15.0pt;font-weight:bold">3）服务降级（服务提供者侧）</span>

<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤1：</span>业务逻辑类写 fallback 方法

使用 `@HystrixCommand` 注解

`fallbackMethod` 指定降级后的处理方法。

~~~java
// 意思为响应超过3秒触发服务降级
commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds",value = "3000")
    }
~~~

~~~java
@HystrixCommand(fallbackMethod = "paymentInfo_TimeoutHandler", commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds",value = "3000")
    })
public String paymentInfo_Timeout(Integer id) {
	int timeNum = 5;  //设置5秒，故意超时
    TimeUnit.SECONDS.sleep(timeNum);
    return "paymentInfo_Timeout，id：" + id + "\t耗时（秒）：" + timeNum;
}

public String paymentInfo_TimeoutHandler(Integer id) {
	return "paymentInfo_TimeoutHandler 服务降级";
}
~~~

<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤2：</span>主启动类开启服务降级

主启动类上加注解 `@EnableCircuitBreaker`。加上 `@EnableCircuitBreaker` 注解之后，就可以使用断路器功能.

~~~java
@SpringBootApplication
@EnableEurekaClient
@EnableCircuitBreaker   
public class PaymentHystrixMain8001 {
    public static void main(String[] args) {
        SpringApplication.run(PaymentHystrixMain8001.class, args);
    }
}
~~~

<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤2：</span>测试

再次访问 http://localhost/consumer/payment/hystrix/timeout/31 就会直接运行服务降级的`Handler`方法。

**结论：**计算异常和超时异常都会触发服务降级。



<span style="color:#0044FF;font-size:15.0pt;font-weight:bold">4）服务降级（服务消费者侧）</span>

<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤1：</span>客户端降级保护

YAML 配置文件：

~~~yaml
feign:
  hystrix:
    enabled: true
~~~

主启动类启动 Hystrix：

~~~java
@EnableHystrix
~~~

业务类：

~~~java
@GetMapping("/consumer/payment/hystrix/timeout/{id}")
@HystrixCommand(fallbackMethod = "paymentTimeOutFallbackMethod", commandProperties = {
        @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "1500")
})
public String paymentInfo_Timeout(@PathVariable("id") Integer id) {
    String result = paymentHystrixService.paymentInfo_Timeout(id);
    return result;
}

public String paymentTimeOutFallbackMethod(@PathVariable("id") Integer id) {
    return "客户端timeout，80服务降级，id：" + id；
}
~~~

<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤2：</span>测试 http://localhost/consumer/payment/hystrix/timeout/31 会直接走80客户端的服务降级处理程序。

<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤3：</span>出现的问题

- 每一个业务方法都写一个服务降级处理程序，**代码膨胀**
- 业务逻辑代码和服务降级处理代码在一起，**代码混乱**



<span style="color:#000000;font-size:14.0pt;font-weight:bold">代码膨胀的解决方法：</span>

在`@RestController`注解上加一个注解`@DefaultProperties(defaultFallback = "")`，意思为没有特别指定就用统一的服务降级处理程序，如果方法上单独指定 `fallbackMethod` 就用单独指定的。（通用的和独享的各自分开，减少代码量）

![](\img\in-post\springcloud\springcloud-07.png)



<span style="color:#000000;font-size:14.0pt;font-weight:bold">代码混乱的解决方法：</span>

耦合度高，只需要为 Feign 客户端定义的接口添加一个服务降级处理的实现类即可实现解耦，重新新建一个类实现该接口，统一为接口里面的方法进行异常处理（将远程过程调用的接口类进行实现）。

在接口上使用`fallback`指定服务降级实现类。



​	**这样虽然解决了代码耦合度问题,但是又出现了过多重复代码的问题,每个方法都有一个降级方法**



> 它的运行逻辑是:
> 	当请求过来,首先还是通过Feign远程调用pay模块对应的方法
>     但是如果pay模块报错,调用失败,那么就会调用PayMentFalbackService类的
>     当前同名的方法,作为降级方法



**远程接口：**

~~~java
@Component
@FeignClient(value = "CLOUD-PROVIDER-HYSTRIX-PAYMENT", fallback = PaymentFallbackService.class)
public interface PaymentHystrixService {
    @GetMapping("/payment/hystrix/ok/{id}")
    public String paymentInfo_OK(@PathVariable("id") Integer id);

    @GetMapping("/payment/hystrix/timeout/{id}")
    public String paymentInfo_Timeout(@PathVariable("id") Integer id);
}
~~~

**实现类：**

~~~java
@Component
public class PaymentFallbackService implements PaymentHystrixService {

    @Override
    public String paymentInfo_OK(Integer id) {
        return "实现类 PaymentFallbackService, paymentInfo_OK fall back";
    }

    @Override
    public String paymentInfo_Timeout(Integer id) {
        return "实现类 PaymentFallbackService, paymentInfo_Timeout fall back";
    }
}
~~~



<span style="color:#0044FF;font-size:15.0pt;font-weight:bold">5）服务熔断</span>（circuitBreaker）

就是保险丝：服务的降级->进而熔断->恢复调用链路

<span style="color:#FF9F00;font-size:12.0pt">服务熔断和服务降级没有关系。调用失败会触发降级，降级会调用fallback方法，降级的流程一定会先调用正常方法再调用fallback方法。假如单位时间内调用失败次数过多，也就是降级次数过多就会出发熔断机制，熔断以后就会跳过正常的方法直接调用fallback方法。</span>



> **熔断机制 **是应对雪崩效应的一种微服务链路保护机制。当扇出链路的某个微服务出错不可用或者响应时间太长时，会进行服务的降级，进而熔断该节点微服务的调用，快速返回错误的响应信息。
> **当检测到该节点微服务调用响应正常后，恢复调用链路。**
>
> 在 Spring Cloud 框架里，熔断机制通过 Hystrix 实现。Hystrix 会监控微服务间调用的状况,当失败的调用到一定阈值，缺省是5秒内20次调用失败，就会启动熔断机制。熔断机制的注解是 `@HystrixCommand`。



服务端代码 service 端配置：在十秒钟的窗口期，10次访问，超过百分之60则服务熔断不可用。

> 在 `class HystrixCommandProperties` 源码类里看可以配置的属性
>
> 其中**三个重要参数：**快照时间窗，请求总数阈值，错误百分比阈值
>
> 1. 快照时间窗：断路器确定是否打开需要统计一些请求和错误数据，而统计的时间范围就是快照时间窗，默认为最近的10秒。
> 2. 请求总数阀值：在快照时间窗内，必须满足请求总数阀值才有资格熔断。默认为20，意味着在10秒内，如果该hystrix命令的调用次数不足20次,即使所有的请求都超时或其他原因失败，断路器都不会打开。
> 3. 错误百分比阀值：当请求总数在快照时间窗内超过了阀值，比如发生了30次调用，如果在这30次调用中，有15次发生了超时异常，也就是超过50%的错误百分比，在默认设定50%阀值情况下，这时候就会将断路器打开.



~~~java
//服务熔断
@HystrixCommand(fallbackMethod = "paymentCircuitBreaker_fallback", commandProperties = {
     @HystrixProperty(name = "circuitBreaker.enabled", value = "true"), //是否开启熔断器
     @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"), //请求次数
     @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "10000"), //时间范围，时间窗口期
     @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "60")  //在窗口期中失败率达到多少后熔断
})
public String paymentCircuitBreaker(@PathVariable("id") Integer id) {
    if (id < 0) {
        throw new RuntimeException("id 要为正数");
    }
    String serialNumber = IdUtil.simpleUUID();
    return Thread.currentThread().getName() + "服务熔断测试调用成功，serialNumber=" + serialNumber;
}
public String paymentCircuitBreaker_fallback(@PathVariable("id") Integer id) {
    return "服务熔断测试调用失败，不能为负数";
}
~~~

服务端代码 controller 端配置：

~~~java
//服务熔断
@GetMapping("/payment/circuit/{id}")
public String paymentCircuitBreaker(@PathVariable("id") Integer id) {
    String result = paymentService.paymentCircuitBreaker(id);
    log.info("result:" + result);
    return result;
}
~~~

<span style="color:#000000;font-size:14.0pt;font-weight:bold">测试：</span>请求 http://localhost:8001/payment/circuit/-31，注意id为-31，疯狂请求，请求一段时间后请求正确的后也不能正确返回，说明已经熔断了。正确几次后就可以正确返回。（从全断开，到半开，最后全开的过程）。

- 熔断打开：请求不再进行调用当前服务，内部设置时钟一般为MTTR(平均故障处理时间)，当打开时长达到所设时钟则进入熔断状态。
- 熔断关闭：熔断关闭不会对服务进行熔断。
- 熔断半开：部分请求根据规则调用当前服务，如果请求成功且符合规则则认为当前服务恢复正常，关闭熔断。



<span style="color:#0044FF;font-size:15.0pt;font-weight:bold">6）服务监控hystrixDashboard</span>

​		除了隔离依赖服务的调用以外，Hystrix 还提供了**准实时的调用监控(Hystrix Dashboard)**，Hystrix 会持续地记录所有通过  Hystrix 发起的请求的执行信息，并以统计报表和图形的形式展示给用户，包括每秒执行多少请求多少成功，多少失败等。Netflix 通过 hystrix-metrics-event-stream 项目实现了对以上指标的监控。Spring Cloud也提供了 Hystrix Dashboard 的整合，对监控内容转化成可视化界面。

<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤1：</span>新建项目

pom 文件：

~~~xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-netflix-hystrix-dashboard</artifactId>
</dependency>
~~~

yml 配置文件：

~~~yaml
server:
  port: 9001
~~~

启动类：

~~~java
@SpringBootApplication
@EnableHystrixDashboard   //新增注解，开启hystrixDashboard
public class HystrixDashboardMain9001 {
    public static void main(String[] args) {
        SpringApplication.run(HystrixDashboardMain9001.class, args);
    }
}
~~~

启动后访问：http://localhost:9001/hystrix，能看到豪猪哥则微服务监控成功。

<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤2：</span>监控服务

所监控的服务都需要依赖 `spring-boot-starter-actuator` 。

给要监控的服务主启动类加一个组件：

~~~java
@SpringBootApplication
@EnableEurekaClient
@EnableCircuitBreaker
public class PaymentHystrixMain8001 {
    public static void main(String[] args) {
        SpringApplication.run(PaymentHystrixMain8001.class, args);
    }

    /**
     * 此配置是为了服务监控而配置，与服务容错本身无关，springcloud升级后的坑
     * ServletRegistrationBean因为Stringboot的默认路径不是“/hystrix.stream”
     * 只要在自己项目里配置上下面的servlet就可以l
     * @return
     */
    @Bean
    public ServletRegistrationBean getServlet(){
        HystrixMetricsStreamServlet streamServlet = new HystrixMetricsStreamServlet();
        ServletRegistrationBean registrationBean = new ServletRegistrationBean(streamServlet);
        registrationBean.setLoadOnStartup(1);
        registrationBean.addUrlMappings("/hystrix.stream");
        registrationBean.setName("HystrixMetricsStreamServlet");
        return registrationBean;
    }
}
~~~

将服务 8001 的地址填写到豪猪哥的地址栏：

![](\img\in-post\springcloud\springcloud-09.png)

使用正确的链接：http://localhost:8001/payment/circuit/31，圆圈会变大。调用几次错误的链接：http://localhost:8001/payment/circuit/-31，圆圈会变成红色，circuit 会打开。

![](\img\in-post\springcloud\springcloud-10.png)

实心圈：共有两种含义。它通过颜色的变化代表了实例的健康程度，它的健康度从绿色<黄色<橙色<红色递减。

该实心圈除了颜色的变化之外，它的大小也会根据实例的请求流量发生变化，流量越大该实心圈就越大。所以通过该实心圈的展示，就可以在大量的实例中快速的发现**故障实例和高压力实例**。



# 4 服务网关



## zuul网关

[官网链接](https://github.com/Netflix/zuul/wiki)，Netflix 公司的，核心成员跳槽，无人维护，市场已经使用较少。



## Gateway网关

[官网链接](https://cloud.spring.io/spring-cloud-static/spring-cloud-gateway/2.2.1.RELEASE/reference/html/)，spring 自研的一套网关，等 zull 等太久了。

<span style="color:#0044FF;font-size:15.0pt;font-weight:bold">1）概念</span>

​		SpringCloud Gateway 是 Spring Cloud 的一个全新项目，基于 Spring 5.0 + Spring Boot 2.0 和 Project Reactor 等技术开发的网关，它旨在为微服务架构提供一种简单有效的统一的API路由管理方式。使用的是 Webflux 中的 reactor-netty 响应式编程组件，底层使用了Netty 通讯框架。

**三大核心概念：**

- **Route（路由）**：路由是构建网关的基本模块，它由ID，目标URI，一系列的断言和过滤器组成，如果断言为true则匹配该路由
- **Predicate（断言）**：参考的是 java8 的 `java.util.function.Predicate` 开发人员可以匹配HTTP请求中的所有内容（例如请求头或请求参数），如果请求与断言相匹配则进行路由
- **Filter（过滤）**：指的是Spring框架中 `GatewayFilter` 的实例，使用过滤器，可以在请求被路由前或者之后对请求进行修改。



**步骤：**

1. 客户端向Spring Cloud Gateway发出请求。然后在Gateway Handler Mapping 中找到与请求相匹配的路由，将其发送到GatewayWeb Handler。
2. Handler再通过指定的过滤器链来将请求发送到我们实际的服务执行业务逻辑，然后返回。过滤器之间用虚线分开是因为过滤器可能会在发送代理请求之前(“pre”)或之后(“post”)执行业务逻辑。
3. Filter在“pre”类型的过滤器可以做参数校验、权限校验、流量监控、日志输出、协议转换等，在“post”类型的过滤器中可以做响应内容、响应头的修改，日志的输出，流量监控等有着非常重要的作用。



<span style="color:#0044FF;font-size:15.0pt;font-weight:bold">2）案例</span>

<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤1：</span>新建模块

pom 依赖：lombok，devtools，starter-test

~~~xml
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-gateway</artifactId>
    <version>2.2.1.RELEASE</version>
</dependency>
        
<!--网关作为一种微服务，也要注册进注册中心-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    <version>2.2.1.RELEASE</version>
</dependency>
~~~

YAML 配置文件：

~~~yaml
server:
  port: 9527
spring:
  application:
    name: cloud-gateway
eureka:
  instance:
    hostname: cloud-gateway-service
  client:    # 服务提供者provider注爵进eureka服务列表内
    service-url:
      defaultZone: http://eureka7001.com:7001/eureka
    register-with-eureka: true
    fetch-registry: true

~~~

主启动类：

~~~java
@SpringBootApplication
@EnableEurekaClient    //网关也是一个微服务，注册进eureka
public class GateWayMain9527 {
    public static void main(String[] args) {
        SpringApplication.run(GateWayMain9527.class, args);
    }
}
~~~

<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤2：</span>路由映射

目前不想暴露 8001 端口（微服务提供者的端口），希望在 8001 外面套一层9527。

将 9527 的 YAML 配置文件修改为：

~~~yaml
server:
  port: 9527
spring:
  application:
    name: cloud-gateway
  cloud:
    gateway:
      routes:
        - id: payment_routh  #payment_route   # 路由的ID，没有固定规则但要求唯一，建议配合服务名
          uri: http://localhost:8001   # 匹配后提供服务的路由地址
          predicates:
            - Path=/payment/get/**     # 自定义断言：如果路径相匹配则进行路由
eureka:
  instance:
    hostname: cloud-gateway-service
  client:    # 服务提供者provider注爵进eureka服务列表内
    service-url:
      defaultZone: http://eureka7001.com:7001/eureka
    register-with-eureka: true
    fetch-registry: true
~~~

9527 启动失败见最后一章踩过的坑4。

<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤3：</span>测试

http://localhost:8001/payment/get/31 正常访问。

淡化真实的服务地址和端口号，访问服务用 http://localhost:9527/payment/get/31 ，也同样调用成功。



<span style="color:#0044FF;font-size:15.0pt;font-weight:bold">3）网关路由两种配置方式</span>

- 在配置文件yml中配置
- 代码中注入RouteLocator的Bean

第一种见上面案例，第二种如下。。还不如第一种配置好写好看。

~~~java
// 以下代码意思为访问localhost:9527/guonei就会自动转发到http://news.baidu.com/guonei
@Configuration
public class GateWayConfig {
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder routeLocatorBuilder){
        RouteLocatorBuilder.Builder routes = routeLocatorBuilder.routes();
        routes.route("path_route_wcy",
                r -> r.path("/guonei")
                        .uri("http://news.baidu.com/guonei")).build();
        return routes.build();
    }
}
~~~



<span style="color:#0044FF;font-size:15.0pt;font-weight:bold">4）通过微服务名实现动态路由</span>

将 9527 的 YMAL 配置修改：

新增了 `spring.cloud.gateway.discovery.locator.enabled = true` ，修改了 `uri `为服务调用名称 `lb://servicename` , gateway 再微服务中自动为我们创建的负载均衡 uri 。

~~~yaml
server:
  port: 9527
spring:
  application:
    name: cloud-gateway
  cloud:
    gateway:
      routes:
        - id: payment_routh  #payment_route   # 路由的ID，没有固定规则但要求唯一，建议配合服务名
          # uri: http://localhost:8001   # 匹配后提供服务的路由地址
          uri: lb://cloud-payment-service  # 匹配后提供服务的路由地址
          predicates:
            - Path=/payment/get/**     # 断言，路径相匹配的进行路由
      discovery:
        locator:
          enabled: true  # 开启从注册中心动态创建路由的功能，利用微服务名进行路由
eureka:
  instance:
    hostname: cloud-gateway-service
  client:    # 服务提供者provider注爵进eureka服务列表内
    service-url:
      defaultZone: http://eureka7001.com:7001/eureka
    register-with-eureka: true
    fetch-registry: true
~~~

测试：访问 http://localhost:9527/payment/get/31 ，8001/8002 两个端口切换。



<span style="color:#0044FF;font-size:15.0pt;font-weight:bold">5）Predicate的使用</span>

predicate 断言，返回值只有 true or false。

说白了，Predicate就是为了实现一组匹配规则，让请求过来找到对应的Route进行处理。

启动网关 9527 后，启动日记里如下：有十多种断言，都是 `RoutePredicateFactory`  工厂生产的。

使用的话参考官网配置（[链接](https://cloud.spring.io/spring-cloud-static/spring-cloud-gateway/2.2.1.RELEASE/reference/html/#gateway-request-predicates-factories)）。

![](\img\in-post\springcloud\springcloud-11.png)



<span style="color:#0044FF;font-size:15.0pt;font-weight:bold">6）Filter的使用</span>

官网详细使用例子（[链接](https://cloud.spring.io/spring-cloud-static/spring-cloud-gateway/2.2.1.RELEASE/reference/html/#gatewayfilter-factories)）

- 指的是Spring框架中GatewayFilter的实例，使用过滤器，可以在请求被路由前或者之后对请求进行修改。
- 路由过滤器可用于修改进入的HTTP请求和返回的HTTP响应，路由过滤器只能指定路由进行使用。
- Spring Cloud Gateway内置了多种路由过战器，他们都由GatewayFilter的工厂类来产生
- 生命周期两个：pre 和 post。
- 种类两个：单一的 GatewayFilter 和 全局的 GlobalFilter。

例子：

~~~yaml
spring:
  cloud:
    gateway:
      routes:
      - id: add_request_header_route
        uri: https://example.org
        filters:
        - AddRequestHeader=X-Request-red, blue  # 请求头加一个请求
~~~



<span style="color:#000000;font-size:14.0pt;font-weight:bold">自定义过滤器例子：</span>

自带的有时候不是很好使，一般使用自定义过滤器。

自定义全局 GlobalFilter。

~~~java
//实现请求参数 uname 有数据才可以访问
@Component
@Slf4j
public class MyLogGateWayFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("MyLogGateWayFilter 我的全局过滤器，时间：" + new Date());
        String uname = exchange.getRequest().getQueryParams().getFirst("uname");
        if (uname == null) {
            log.info("用户名为空");
            exchange.getResponse().setStatusCode(HttpStatus.NOT_ACCEPTABLE);
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {   //返回优先级，数字越小优先级越高
        return 0;
    }
}
~~~

**测试：**重启服务后 访问 http://localhost:9527/payment/get/31 不能正常访问，访问http://localhost:9527/payment/get/31?uname=1 可正常访问，过滤器生效。







# 5 服务配置



## Config分布式配置中心

SpringCloud Config，[官网](https://cloud.spring.io/spring-cloud-static/spring-cloud-config/2.2.1.RELEASE/reference/html/)



<span style="color:#0044FF;font-size:15.0pt;font-weight:bold">1）概念</span>



微服务意味着要将单体应用中的业务拆分成一个个子服务，每个服务的粒度相对较小，因此系统中会出现大量的服务。由于每个服务都需要必要的配置信息才能运行，所以一套<span style="color:#FF0000">集中式的、动态的配置管理设施</span>是必不可少的。



SpringCloud Config 为微服务架构中的微服务提供集中化的外部配置支持，配置服务器为<span style="color:#FF0000">各个不同微服务应用</span>的所有环境提供了一个<span style="color:#FF0000">中心化的外部配置（Git）</span>。



![](\img\in-post\springcloud\springcloud-12.png)



- 分为服务端，客户端两部分。
- **服务端**也称为分布式配置中心，它是一个独立的微服务应用，用来连接配置服务器并为客户端提供获取配置信息，加密/解密信息等访问接口。
- **客户端**则是通过指定的配置中心来管理应用资源，以及与业务相关的配置内容，并在启动的时候从配置中心获取和加载配置信息配置服务器默认采用git来存储配置信息，这样就有助于对环境配置进行版本管理，并且可以通过git客户端工具来方便的管理和访问配置内容。



**配置中心能干什么：**

1. 集中管理配置文件
2. 不同环境不同配置，动态化的配置更新，分环境部署比如dev/test/prod/beta/release
3. 运行期间动态调整配置，不再需要在每个服务部署的机器上编写配置文件，服务会向配置中心统一拉取配置自己的信息
4. 当配置发生变动时，服务不需要重启即可感知到配置的变化并应用新的配置
5. 将配置信息以REST接口的形式暴露



<span style="color:#0044FF;font-size:15.0pt;font-weight:bold">2）Config服务端配置与测试</span>

图中的 `Config Server` 。



**步骤：**

1. 用你自己的账号在Github上新建一个名为sprincloud-config的新Repository

2. 由上一步获得刚新建的git地址

3. 本地硬盘上新建git仓库并clone `git clone  xxx` 

4. 此时在本地盘符下path，保存路径必须为UTF-8

5. 新建Module模块cloud-config-center-3344它既为Cloud的配置中心模块cloudConfig Center

   - 建module

   - pom 依赖：

     ~~~xml
     <dependency>
     	<groupId>org.springframework.cloud</groupId>
     	<artifactId>spring-cloud-config-server</artifactId>
     </dependency>
     <dependency>
     	<groupId>org.springframework.cloud</groupId>
     	<artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
     	<version>2.2.1.RELEASE</version>
     </dependency>
     ~~~

   - YAML 配置文件：

     ~~~yaml
     server:
       port: 3344
     
     spring:
       application:
         name: cloud-config-center # 注册进Eureka服务器的微服务名
       cloud:
         config:
           server:
             git:
               uri: https://github.com/strato-sail/springcloud-config.git #GitHub上的git仓库地址
               search-paths:   # 搜索目录
                 - springcloud-config
           label: main  # 读取分支
     
     eureka:
       client:
         service-url:
           defaultZone: http://localhost:7001/eureka
     ~~~

   - 主启动类

     ~~~java
     @SpringBootApplication
     @EnableConfigServer
     public class ConfigCenterMain3344 {
         public static void main(String[] args) {
             SpringApplication.run(ConfigCenterMain3344.class, args);
         }
     }
     ~~~

6. 增加 hosts 映射：`127.0.0.1  config-3344.com`

7. 测试通过 Config 微服务是否可以从 GitHub 上获取配置内容

   - 先启动 7001 注册中心，后启动微服务3344。
   - 访问 http://config-3344.com:3344/master/config-dev.yml。



**配置读取规则：**

> label：分支 branch，name：服务名，profiles：环境（dev/test/prod）

- `/{label}/{application}-{profile}.yml` （最推荐使用这种方式）
  - master 分支：http://config-3344.com:3344/master/config-dev.yml
  - dev 分支：http://config-3344.com:3344/dev/config-dev.yml
- `/{application}-{profile}[/{label}]` ，返回的是 `json` 串。
  - master 分支：http://config-3344.com:3344/config/prod/master
  - dev 分支：http://config-3344.com:3344/config/dev/dev



<span style="color:#0044FF;font-size:15.0pt;font-weight:bold">3）Config客户配置与测试</span>



<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤：</span>建新 module

- pom 依赖：

  ~~~xml
  <dependency>
  	<groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-config</artifactId>
  </dependency>
  <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
      <version>2.2.1.RELEASE</version>
  </dependency>
  ~~~

- YAML 配置文件：使用 `bootstrap.yaml` 

  > applicaiton . ym1是用户级的资源配置项
  >
  > bootstrap.ym1是系统级的**，优先级更加高**
  >
  > Spring Cloud会创建一个“Bootstrap Context”，作为Spring应用的Application Context的**父上下文**。初始化的时候，BootstrapContext 负责**从外部源加载配置属性并解析配置**。这两个上下文共享一个从外部获取的 Environment。
  >
  > Bootstrap 属性有高优先级，默认情况下，它们**不会被本地配置覆盖**。Bootstrap context 和Application Context 有着不同的约定，所以新增了一个 bootstrap.yml 文件，保证 Bootstrap Context 和 Application Context 配置的分离。
  >
  > 要将Client模块下的application.yml文件改为bootstrap.yml,这是很关键的，因为bootstrap.yml是比application.yml先加载的。bootstrap.yml优先级高于application.yml

  ~~~yaml
  server:
    port: 3355
  
  spring:
    application:
      name: config-client
    cloud:
      # config客户端配置
      config:
        label: main  # 分支名称
        name: config  # 配置文件名称
        profile: dev  # 读取后缀名称
        # 上述三个综合：main分支上的config-dev.yml的配置文件被读取 http://config-3344.com:3344/master/config-dev.yml
        uri: http://localhost:3344  # 配置中心地址
  
  eureka:
    client:
      service-url:
        defaultZone: http://eureka7001.com:7001/eureka
  ~~~

- 主启动类：

  ~~~java
  @SpringBootApplication
  @EnableEurekaClient
  public class ConfigClientMain3355 {
      public static void main(String[] args) {
          SpringApplication.run(ConfigClientMain3355.class, args);
      }
  }
  ~~~

- 业务类：

  ~~~java
  @RestController
  public class ConfigClientController {
  
      @Value("${config.info}")
      private String configInfo;
  
      @GetMapping("/configInfo")
      public String getConfigInfo() {
          return configInfo;
      }
  }
  ~~~

- 测试： http://localhost:3355/configInfo 正确拿到，成功实现了客户端3355访问SpringCloud Config3344通过GitHub获取配置信息。



<span style="color:#000000;font-size:14.0pt;font-weight:bold">手动动态刷新配置：</span>

**问题：**改了 GitHub 上的配置文件后，刷新 3344，发现 ConfigServer 配置中心立刻响应，但是刷新 3355，发现 ConfigServer 客户端没有任何响应，3355 没有变化除非自己重启或者重新加载。得重启 3355，配置才更新。

**目标：**避免每次更新配置都要重启客户端微服务 3355



<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤：</span>

- 修改 3355 模块，添加依赖：

  ~~~xml
  <dependency>
  	<groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-actuator</artifactId>
  </dependency>
  ~~~

- 修改 YML 配置文件，暴露监控的端口：

  ~~~yaml
  # 暴露监控端点
  management:
    endpoints:
      web:
        exposure:
          include: "*"
  ~~~

- 业务类添加注解 `@RefreshScope` 刷新的注解 ，这个注解主要是在含有 `@Value` 的类上面。

  ~~~java
  @RestController
  @RefreshScope
  public class ConfigClientController {
  
      @Value("${config.info}")
      private String configInfo;
  
      @GetMapping("/configInfo")
      public String getConfigInfo() {
          return configInfo;
      }
  }
  ~~~

- 测试：先修改 github 上的值，然后看 3344 是否刷新，最后看 3355 是否刷新--------->没有变化

- 需要运维人员发送Post请求刷新 3355：请求

  ~~~shell
  curl -X POST "http://localhost:3355/actuator/refresh"
  ~~~

- 现在再测试就成功了。避免了服务重启（写一个自动化脚本就ok）





# 6 服务总线



## Bus消息总线

Springcloud Bus 消息总线。



Spring Cloud Bus能管理和传播分布式系统间的消息，就像一个分布式执行器，可用于广播状态更改、事件推送等，也可以当作微服务间的通信通道。



**什么是总线：**

在微服务架构的系统中，通常会使用<span style="color:#FF0000">轻量级的消息代理</span>来构建一 个<span style="color:#FF0000">共用的消息主题</span>，并让系统中所有微服务实例都连接上来。<span style="color:#FF0000">由于该主题中产生的消息会被所有实例监听和消费，所以称它为消息总线</span>。在总线上的各个实例，都可以方便地厂播- -些需要让其他连接在该主题上的实例都知道的消息。



**基本原理：**

ConfigClient实例都监听MQ中同一 个topic(默认是springCloudBus)。 当-个服务刷新数据的时候,它会把这个信息放入到Topic中,这样其它监听同一Topic的服务就能得到通知，然后去更新自身的配置。



<span style="color:#0044FF;font-size:15.0pt;font-weight:bold">1）概念</span>

- 分布式自动刷新配置功能
- Spring Cloud Bus 配合 Spring Cloud Config 使用可以实现配置的动态刷新。
- Bus 支持两种消息代理：RabbitMQ（队列加 Topic 主题） 和 Kafka



![](\img\in-post\springcloud\springcloud-13.png)



<span style="color:#0044FF;font-size:15.0pt;font-weight:bold">2）RibbitMQ配置</span>

安装RibbitMQ。<span style="color:#FF0000">安装的时候一定要注意 erlang 和 rabbitmq-server 的版本对应关系。</span>

![](\img\in-post\springcloud\springcloud-14.png)



<span style="color:#0044FF;font-size:15.0pt;font-weight:bold">3）Bus动态刷新全局广播</span>

<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤：</span>

- 为了增加广播效果，和 3355 相同创建一个 3366 module。

- 设计思想：

  1. 利用消息总线触发一个客户端/bus/refresh,而刷新所有客户端的配置（上面概念中的图）
  2. 利用消息总线触发一个服务端ConfigServer的/bus/refresh端点,而刷新所有客户端的配置（更加推荐，下图）

  ![](\img\in-post\springcloud\springcloud-15.png)

- 给cloud-config-center-3344配置中心服务端添加消息总线支持

  - POM 添加 RabbitMQ 依赖：<span style="color:#FF0000">有坑，见问题5</span>

    ~~~xml
    <!-- https://mvnrepository.com/artifact/org.springframework.cloud/spring-cloud-starter-bus-amqp -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-bus-amqp</artifactId>
    </dependency>
    ~~~

  - YAML 配置文件添加：

    ~~~yaml
    spring:
      # rabbitmq相关配置
      rabbitmq:
        host: localhost
        port: 5672
        username: guest
        password: guest
    
    # rabbitmq相关配置，暴露bus刷新配置的端点
    management:
      endpoints:   # 暴露bus刷新配置的端点
        web:
          exposure:
            include: 'bus-refresh'
    ~~~

- 给cloud-config-center-3355客户端添加消息总线支持

  - POM 依赖：

    ~~~xml
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-bus-amqp</artifactId>
    </dependency>
    ~~~

  - YAML 配置文件：

    ~~~yaml
    spring:
      # rabbitmq相关配置
      rabbitmq:
        host: localhost
        port: 5672
        username: guest
        password: guest
    
    # 暴露监控端点
    management:
      endpoints:
        web:
          exposure:
            include: "*"   # 不refresh，在3344上refresh
    ~~~

- 给cloud-config-center-3366客户端添加消息总线支持，同上 3355。

- **测试：**修改远程配置文件后只需要刷新 3344 就可以实现一次发送，处处生效。`curl -X POST "http://localhost:3344/actuator/bus-refresh"`

- 登录到 RabbitMQ 管理界面，在 `Exchange`下面有个 `springCloudBus topic`就是总线订阅的主题。 



<span style="color:#0044FF;font-size:15.0pt;font-weight:bold">4）Bus动态刷新定点通知</span>

不同于全局广播，我只想通知客户端中的一个。

**公式：**`http://localhost:配置中心的端口号/actuator/bus-refresh/{destination}`

`/bus/refresh` 请求不再发送到具体的服务实例上，而是发给 config server 并通过 `destination` 参数类指定需要更新配置的服务或实例

`destination `就是 `spring.application.name : server.port`，例如下面代码：

~~~yaml
# destination就是 config-client:3366
server:
  port: 3366

spring:
  application:
    name: config-client
~~~



**需求：**只通知 3355，不通知 3366。

**步骤：**

- 修改远程仓库的配置文件
- 发送 post 请求到3344配置中心：`curl -X POST "http://localhost:3344/actuator/bus-refresh/config-client:3355"`

**总结：**

![](\img\in-post\springcloud\springcloud-16.png)



# 7 消息驱动



## Cloud Stream消息驱动

[官网](https://spring.io/projects/spring-cloud-stream#overview)，[官网操作手册](https://cloud.spring.io/spring-cloud-static/spring-cloud-stream/3.0.1.RELEASE/reference/html/)，[中文指导手册](https://m.wang1314.com/doc/webapp/topic/20971999.html)

<span style="color:#FF0000">目前SpringCloud Stream仅支持RabbitMQ、Kafka。</span>



<span style="color:#0044FF;font-size:15.0pt;font-weight:bold">1）概念</span>

- 让我们不再关注具体 MQ 的细节，我们只需要用一种适配绑定的方式，自动在各种 MQ 内切换。
- Cloud Stream 屏蔽了底层消息中间件的差异，降低切换的成本，统一消息的编程模型，中间层类似 JDBC。
- 官方定义Spring Cloud Stream是一个构建消息驱动微服务的框架。



如果一个系统里使用多种 MQ，此时消息队列和系统就耦合在一起了，涉及到两中消息中间件直接的数据迁移，就是灾难性的。SpringCloud Stream 提供了一种解耦合的方式。



应用程序通过 inputs 或者 outputs 来与Spring Cloud Stream 中 binder 对象交互。通过我们配置来binding(绑定)，而Spring Cloud Stream的<span style="color:#FF0000"> binder 对象负责与消息中间件交互</span>。所以，我们只需要搞清楚如何与Spring Cloud Stream交互就可以方便使用消息驱动的方式。



![](F:\strato-sail\strato-sail.GitHub.io\img\in-post\springcloud\springcloud-17.png)



| 组成            | 说明                                                         |
| --------------- | ------------------------------------------------------------ |
| Middleware      | 中间件，目前只支持RabbitMQ 和 Kafka                          |
| Binder          | Binder是应用与消息中间件之间的封装，目前实行了Kafka和RabbitMQ的Binder，通过Binder可以很方便的连接中间件，可以动态的改变消息类型(对嗓于Kafka的topic, RabbitMQ 的 exchange)，这些都可以通过配置文件来实现 |
| @Input          | 注解标识输入通道，通过该输入通道接收到的消息进入应用程序     |
| @Output         | 注解标识输出通道，发布的消息将通过该通道离开应用程序         |
| @StreamListener | 监听队列，用于消费者的队列的消息接收                         |
| @EnableBinding  | 指信道channel和exchange绑定在一起                            |



其实Springcloud Stream 就是 Topic 主题发布订阅模式，在RabbitMQ 中就是Exchange，在Kafka 种就是 Topic。通过定义绑定器Binder 作为中间层，实现了应用程序与消息中间件细节之间的隔离。



![](F:\strato-sail\strato-sail.GitHub.io\img\in-post\springcloud\springcloud-18.png)



**上图中的概念：**

- Binder：很方便的连接中间件，屏蔽差异
- Channel：通道，是队列Queue的一种抽象，在消息通讯系统中就是实现存储和转发的媒介，通过对Channel对队列进行配置
- Source 和 Sink：简单的可理解为参照对象是Spring Cloud Stream自身，从Stream发布消息就是输出，接受消息就是输入



<span style="color:#0044FF;font-size:15.0pt;font-weight:bold">2）案例实践</span>

![](F:\strato-sail\strato-sail.GitHub.io\img\in-post\springcloud\springcloud-19.png)



- 配置文件声明Binder的主题名和Channel
- 消息生产者使用`org.springframework.messaging.MessageChannel`发送消息`output.send(MessageBuilder.withPayload(serial).build())`
- 消息消费者绑定`org.springframework.cloud.stream.messaging.Sink`并接收



<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤1：</span>消息驱动生产者

- POM 依赖：

  ~~~xml
  <dependency>
  	<groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-stream-rabbit</artifactId>
  </dependency>
  ~~~

- YAML 配置

  ~~~yaml
  server:
    port: 8801
  spring:
    application:
      name: cloud-stream-provider
    cloud:
      stream:
        binders:  # 此处配置要绑定的rabbitmq的服务信息
          defaultRabbit:  # 表示定义的名称，用于和binding整合
            type: rabbit  # 消息组件类型
            environment:  # 设置rabbitmq的相关的环境配置
              spring:
                rabbitmq:
                  host: localhost
                  port: 5672
                  username: guest
                  password: guest
        bindings:  # 服务的整合处理
          output:  # 这个名字是一个通道名称
            destination: studyExchange  # 表示要使用的Exchange名称定义
            content-type: application/json  # 设置消息类型、本次为json，文本则设置“text/plain”
            binder: defaultRabbit  # 设置要绑定的消息服务的具体设置
  eureka:
    client:
      service-url:
        defaultZone: http://localhost:7001/eureka
    instance:
      lease-renewal-interval-in-seconds: 2  # 设置心跳的时间间隔（默认是30秒）
      lease-expiration-duration-in-seconds: 5  # 如果现在超过了5秒时间间隔（默认是90秒）
      instance-id: send-9901.com  # 在信息列表时显示主机名称
      prefer-ip-address: true  # 访问的路径变为IP地址
  ~~~

- 主启动类：

  ~~~java
  @SpringBootApplication
  public class StreamMQMain8801 {
      public static void main(String[] args) {
          SpringApplication.run(StreamMQMain8801.class, args);
      }
  }
  ~~~

- 业务逻辑类，只是搭建通道，没有 dao，操作的是消息中间件（看生产者消费者上图组织结构，引入的 `Source.class` 是 `cloud.stream.messaging.Source` 的）

  - 接口：

    ~~~java
    package com.wcy.springcloud.servuce;
    
    public interface IMessageProvider {
        public String send();
    }
    ~~~

  - 实现类：

    ~~~java
    package com.wcy.springcloud.servuce.impl;
    
    import com.wcy.springcloud.servuce.IMessageProvider;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.cloud.stream.annotation.EnableBinding;
    import org.springframework.cloud.stream.messaging.Source;
    import org.springframework.messaging.MessageChannel;
    import org.springframework.messaging.support.MessageBuilder;
    import java.util.UUID;
    
    @EnableBinding(Source.class)  //定义消息的推送管道
    public class MessageProviderImpl implements IMessageProvider {
    
        @Autowired
        private MessageChannel output; //消息发送管道
    
        @Override
        public String send() {
            String serial = UUID.randomUUID().toString();
            output.send(MessageBuilder.withPayload(serial).build());
            System.out.println("serial:"+serial);
            return null;
        }
    }
    ~~~

  - controller层：

    ~~~java
    @RestController
    public class SendMessageController {
        @Autowired
        private MessageProviderImpl messageProvider;
    
        @GetMapping("/sendMessage")
        public String sendMessage(){
            return messageProvider.send();
        }
    }
    ~~~

- 测试：启动 7001 注册中心，启动 rabbitmq（在 http://localhost:15672/#/exchanges 里就有上面 yaml 配置文件配置的 studyExchange 的主题 Topic），启动 8801，访问。



<span style="color:#000000;font-size:14.0pt;font-weight:bold">步骤2：</span>消息驱动消费者

- POM 依赖：

  ~~~xml
  <dependency>
  	<groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-stream-rabbit</artifactId>
  </dependency>
  ~~~

- YAML 配置文件：

  ~~~yaml
  server:
    port: 8802
  spring:
    application:
      name: cloud-stream-consumer
    cloud:
      stream:
        binders:  # 此处配置要绑定的rabbitmq的服务信息
          defaultRabbit:  # 表示定义的名称，用于和binding整合
            type: rabbit  # 消息组件类型
            environment:  # 设置rabbitmq的相关的环境配置
              spring:
                rabbitmq:
                  host: localhost
                  port: 5672
                  username: guest
                  password: guest
        bindings:  # 服务的整合处理
          input:  # 这个名字是一个通道名称
            destination: studyExchange  # 表示要使用的Exchange名称定义
            content-type: application/json  # 设置消息类型、本次为json，文本则设置“text/plain”
            binder: defaultRabbit  # 设置要绑定的消息服务的具体设置
  eureka:
    client:
      service-url:
        defaultZone: http://localhost:7001/eureka
    instance:
      lease-renewal-interval-in-seconds: 2  # 设置心跳的时间间隔（默认是30秒）
      lease-expiration-duration-in-seconds: 5  # 如果现在超过了5秒时间间隔（默认是90秒）
      instance-id: receive-8802.com  # 在信息列表时显示主机名称
      prefer-ip-address: true  # 访问的路径变为IP地址
  ~~~

- 主启动类：

  ~~~~java
  @SpringBootApplication
  public class StreamMQMain8802 {
      public static void main(String[] args) {
          SpringApplication.run(StreamMQMain8802.class, args);
      }
  }
  ~~~~

- 业务类 controller：

  ~~~java
  package com.wcy.springcloud.controller;
  
  import org.springframework.beans.factory.annotation.Value;
  import org.springframework.cloud.stream.annotation.EnableBinding;
  import org.springframework.cloud.stream.annotation.StreamListener;
  import org.springframework.cloud.stream.messaging.Sink;
  import org.springframework.messaging.Message;
  import org.springframework.stereotype.Component;
  
  /**
   * @author wangcy
   * @date 2021-5-17 11:35
   * descriotion
   */
  @Component
  @EnableBinding(Sink.class)
  public class ReceiveMessageListenerController {
  
      @Value("${server.port}")
      private String serverPort;
  
      @StreamListener(Sink.INPUT)
      public void input(Message<String> message) {
          System.out.println("消费者1号，接受到的消息：" + message.getPayload() + "\t prot:" + serverPort);
      }
  }
  ~~~

- 测试：访问 http://localhost:8801/sendMessage 发送消息后，8802消息消费者收到消息。



<span style="color:#0044FF;font-size:15.0pt;font-weight:bold">3）重复消费的问题</span>

不同组是可以全面消费的（重复消费）；同一组内会发生竞争关系，只有其中一个可以消费。

- 故障现象：重复消费
- 导致原因：默认分组group是不同的，组流水号是不同的，被认为是不同组
- 自定义配置分组，分为一个组解决

<span style="color:#FF0000">散服务应用放置于同一个group中，就能够保证消息只会被其中一个应用消费一次。不同的组是可以消费的，同一个组内会发生竞争关系，只有其中一个可以消费。</span>



<span style="color:#000000;font-size:14.0pt;font-weight:bold">更改组步骤：</span>

YAML 配置改变：每个组相当于一个队列，相同组使用一个队列

~~~yaml
bindings:  # 服务的整合处理
  input:  # 这个名字是一个通道名称
    destination: studyExchange  # 表示要使用的Exchange名称定义
    content-type: application/json  # 设置消息类型、本次为json，文本则设置“text/plain”
    binder: defaultRabbit  # 设置要绑定的消息服务的具体设置
    group: wcyTEST1  # 自定义组名
~~~



<span style="color:#0044FF;font-size:15.0pt;font-weight:bold">4）持久化的问题</span>

增加 `group` 的属性配置，自动实现了持久化的问题。

<span style="color:#000000;font-size:14.0pt;font-weight:bold">测试步骤：</span>

- 将8802 消费者的配置文件 group 属性删掉
- 8801 生产者多生产几个消息
- 先启动8802，无分组属性配置，消息丢失
- 再启动8803，有分组属性配置，后台打出消息





# # 踩过的坑

<span style="color:#0000FF;font-size:14.0pt;font-weight:bold">问题1：</span>服务降级报错

Hystrix 配置服务降级的时候，将远程过程调用的 Service 的注解 `@Component` 不小心写成了 `@Controller` ，在加了远程过程接口的实现类后，结果报错：

~~~shell
com.wcy.springcloud.service.PaymentHystrixService#paymentInfo_Timeout(Integer)
to {GET /payment/hystrix/timeout/{id}}: There is already 'paymentFallbackService' bean method
~~~

报错信息是映射路径 mapper 重复了。

**另外收获：**网上博客（[地址](https://my.oschina.net/u/2000675/blog/2244769)）说 `@RequestMapping ` 不能加载接口类上，否则也会报错，得加在接口的类上。`



<span style="color:#0000FF;font-size:14.0pt;font-weight:bold">问题2：</span>服务降级没有识别到默认fall back 方法

这是因为指定的 备用方法 和 原方法 的参数个数，类型不同造成的，要统一参数的类型和个数。

参考：[博客地址](https://blog.csdn.net/zhangminemail/article/details/84939595)



<span style="color:#0000FF;font-size:14.0pt;font-weight:bold">问题3：</span>Hystrix 服务监控要加一个组件，否则监控失败 404

~~~java
/**
 * 此配置是为了服务监控而配置，与服务容错本身无关，springcloud升级后的坑
 * ServletRegistrationBean因为Stringboot的默认路径不是“/hystrix.stream”
 * 只要在自己项目里配置上下面的servlet就可以l
 * @return
 */
@Bean
public ServletRegistrationBean getServlet(){
	HystrixMetricsStreamServlet streamServlet = new HystrixMetricsStreamServlet();
    ServletRegistrationBean registrationBean = new ServletRegistrationBean(streamServlet);
    registrationBean.setLoadOnStartup(1);
    registrationBean.addUrlMappings("/hystrix.stream");
    registrationBean.setName("HystrixMetricsStreamServlet");
    return registrationBean;
}
~~~



<span style="color:#0000FF;font-size:14.0pt;font-weight:bold">问题4：</span>springcloud-gateway 启动失败，原因是 gateway 不是 web 项目，需要排除 starter-web 的依赖，将下面两个依赖删除：

~~~xml
<dependency>
	<groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
~~~



<span style="color:#0000FF;font-size:14.0pt;font-weight:bold">问题5：</span>学习springcloud BUS的时候，导入依赖 `spring-cloud-starter-bus-amqp` 出错爆红，原因是<span style="color:#FF0000">我项目中的maven配置的是阿里云的仓库，但是阿里云仓库中关于springcloud的jar包不完整，需要改为国外的下载地址</span>

解决方案：https://blog.csdn.net/u013456390/article/details/109428974

将阿里云仓库改为国外地址。



