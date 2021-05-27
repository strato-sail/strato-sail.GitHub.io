---
layout:     post
title:      "SpringCloud Alibaba使用学习笔记"
subtitle:   "Learn about spring cloud alibaba"
date:       2021-05-17 12:00:00
author:     "Wangcy"
catalog: false
header-style: text
tags:
  - spring 
  - springCloud
  - 分布式
  - 微服务
  - Nacos
---



<span style="color:#000000;font-size:14.0pt;font-weight:bold">SpringCloud Alibaba简介</span>



[通知](https://spring.io/blog/2018/12/12/spring-cloud-greenwich-rc1-available-now)说明自2018年12月12日起，Spring Cloud Netflix 将不再开发新的组件，进入了维护阶段。

2018.10.31，Spring Cloud Alibaba 正式入驻了Spring Cloud官方孵化器，并在Maven 中央库发布了第一个版本。

[中文手册](https://github.com/alibaba/spring-cloud-alibaba/blob/master/README-zh.md)、[英文操作手册](https://spring.io/projects/spring-cloud-alibaba#overview)



<span style="color:#000000;font-size:14.0pt;font-weight:bold">SpringCloud Alibaba 能干什么：</span>

- **服务限流降级：**默认支持 Servlet、Feign、RestTemplate、Dubbo和RocketMQ限流降级功能的接入，可以在运行时通过控制台实时修改限流降级规则，还支持查看限流降级Metrics监控。
- **服务注册与发现：**适配 Spring Cloud服务注册与发现标准，默认集成了Ribbon的支持。
- **分布式配置管理：**支持分布式系统中的外部化配置，配置更改时自动刷新。
- **消息驱动能力：**基于Spring Cloud Stream为微服务应用构建消息驱动能力。
- **阿里云对象存储：**阿里云提供的海量、安全、低成本、高可靠的云存储服务。支持在任何应用、任何时间、任何地点存储和访问任意类型的数据.
- **分布式任务调度：**提供秒级、精准、高可靠、高可用的定时(基于Cron表达式)任务调度服务。同时提供分布式的任务执行模型，如网格任务。网格任务支持海量子任务均匀分配到所有Worker (schedulerx-client)上执行。



# 1 Nacos 服务注册和配置中心

SpringCloud Alibaba Nacos

[源码](https://github.com/alibaba/Nacos)、[官网](https://nacos.io/zh-cn/)、[官方学习手册](https://spring-cloud-alibaba-group.github.io/github-pages/greenwich/spring-cloud-alibaba.html#_spring_cloud_alibaba_nacos_discovery)



## 1.1 简介、安装与运行



- 一个更易于构建云原生应用的动态服务发现，配置管理和服务管理中心
- Nacos = Eureka+Config+Bus（Nacos就是注册中心+配置中心的组合）



**安装启动步骤：**

- 从[官网](https://nacos.io/zh-cn/)下载，解压后
- 根据遇见的坑1，完成nacos的运行成功
- 默认账号密码为nacos



相比较于 Nacos，Eureka 服务注册中心需要自己下载搭建。Nacos 直接集成了后台和前端管理页面。Nacos 支持 AP 模式和 CP 模式的切换。



## 1.2 使用案例



<span style="color:#0044FF;font-size:15.0pt;font-weight:bold">1）Nacos 作为服务注册中心</span>

替代 Eureka。

[官方指导手册](https://spring.io/projects/spring-cloud-alibaba#learn)



<span style="color:#000000;font-size:14.0pt;font-weight:bold">基于Nacos的服务提供者</span>

- 新建 Module ：cloudalibaba-provider-payment9001

- POM 依赖：

  - 在父 POM 引入了阿里巴巴的依赖

    ~~~xml
    <!--spring cloud 阿里巴巴-->
    <dependency>
    	<groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-alibaba-dependencies</artifactId>
        <version>2.1.0.RELEASE</version>
        <type>pom</type>
        <scope>import</scope>
    </dependency>
    ~~~

  - 在本模块 POM 引入：

    ~~~xml
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
    </dependency>
    ~~~

- YAML 配置文件：

  ~~~yaml
  server:
    port: 9001
  
  spring:
    application:
      name: nacos-payment-provider
    cloud:
      nacos:
        discovery:
          server-addr: localhost:8848  # 配置Nacos地址
  management:
    endpoints:
      web:
        exposure:
          include: '*'
  ~~~

- 主启动类：

  ~~~java
  @SpringBootApplication
  @EnableDiscoveryClient
  public class PaymentMain9001 {
      public static void main(String[] args) {
          SpringApplication.run(PaymentMain9001.class, args);
      }
  }
  ~~~

- 业务类：

  ~~~java
  @RestController
  public class PaymentController {
      @Value("${server.port}")
      private String serverPort;
      @GetMapping("/payment/nacos/{id}")
      public String getPayment(@PathVariable("id") Integer id) {
          return "nacos registry,serverPort:" + serverPort + "\tid:" + id;
      }
  }
  ~~~

- 启动并测试，一切ok

- nacos 自带负载均衡，参照上面9001，新建9002





<span style="color:#000000;font-size:14.0pt;font-weight:bold">基于Nacos的服务消费者</span>

- 新建 Module：cloudalibaba-consumer-nacos-order83

- POM 依赖：

  ~~~xml
  <dependency>
      <groupId>com.alibaba.cloud</groupId>
      <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
  </dependency>
  ~~~

- YAML 配置文件

  ~~~yaml
  server:
    port: 83
  
  spring:
    application:
      name: nacos-order-consumer
    cloud:
      nacos:
        discovery:
          server-addr: localhost:8848  # 配置Nacos地址
  # 消费者将要去访问的微服务名称（注册成功进nacos的微服务提供者）
  service-url:
    nacos-user-service: http://nacos-payment-provider
  ~~~

- 主启动类

  ~~~java
  @EnableDiscoveryClient
  @SpringBootApplication
  public class OrderNacosMain83 {
      public static void main(String[] args) {
          SpringApplication.run(OrderNacosMain83.class, args);
      }
  }
  ~~~

- 业务类

  - nacos-discovery 依赖引入了 ribbon，ribbon 需要restTemplate：

    ~~~java
    @Configuration
    public class ApplicationContextConfig {
        @Bean
        @LoadBalanced  //负载均衡
        public RestTemplate getRestTemplate(){
            return new RestTemplate();
        }
    }
    ~~~

  - controller 业务类：

    ~~~java
    @RestController
    @Slf4j
    public class OrderNacosController {
    
        @Autowired
        private RestTemplate restTemplate;
    
        @Value("${service-url.nacos-user-service}")
        private String serverURL;
    
        @GetMapping("/consumer/payment/nacos/{id}")
        public String paymentInfo(@PathVariable("id") Integer id) {
            return restTemplate.getForObject(serverURL + "/payment/nacos/" + id, String.class);
        }
    }
    ~~~

- 测试，自带负载均衡



<span style="color:#0044FF;font-size:15.0pt;font-weight:bold">2）Nacos 作为服务配置中心</span>

替代SpringCloud Config。

[官方指导手册](https://spring.io/projects/spring-cloud-alibaba#learn)



<span style="color:#000000;font-size:14.0pt;font-weight:bold">基础配置</span>

- 建Module：cloudalibaba-config-nacos-client3377

- POM 依赖：

  ~~~xml
  <dependency>
      <groupId>com.alibaba.cloud</groupId>
      <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
  </dependency>
  <dependency>
      <groupId>com.alibaba.cloud</groupId>
      <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
  </dependency>
  ~~~

- YML 配置文件：两个配置文件，优先级bootstrap 优先级高于 application。下面两个拼起来表示去8848找一个dev.yaml配置文件。

  - bootstrap.yaml：

    ~~~yaml
    server:
      port: 3377
    spring:
      application:
        name: nacos-config-client
      cloud:
        nacos:
          discovery:
            server-addr: localhost:8848  # Nacos服务注册中心地址
          config:
            server-addr: localhost:8848  # Nacos服务配置中心地址
            file-extension: yaml  # 指定yaml格式的配置
    ~~~

  - application.yaml：

    ~~~yaml
    spring:
      profiles:
        active: dev # 表示开发环境
    ~~~

- 主启动类：

  ~~~java
  @EnableDiscoveryClient
  ~~~

- 业务类：通过 SpringCloud原生注解`@RefreshScope` 实现配置的自动更新

  ~~~java
  @RestController
  @RefreshScope    //支持Nacos的动态刷新功能
  public class ConfigClientController {
      @Value("${config.info}")
      private String configInfo;
  
      @GetMapping("/config/info")
      public String getConfigInfo() {
          return configInfo;
      }
  }
  ~~~

- 在Nacos中添加配置信息，在 http://localhost:8848/nacos/ 下新增配置 `nacos-config-client-dev.yaml` 

  ![](\img\in-post\springcloud\springcloud-alibaba-01.png)

  > Nacos中的匹配规则，在 Nacos Spring Cloud 中，`dataId` 的完整格式如下：
  >
  > ~~~xml
  > ${prefix}-${spring.profiles.active}.${file-extension}
  > ~~~
  >
  > - `prefix` 默认为 `spring.application.name` 的值，也可以通过配置项 `spring.cloud.nacos.config.prefix`来配置。
  > - `spring.profiles.active` 即为当前环境对应的 profile，详情可以参考 [Spring Boot文档](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-profiles.html#boot-features-profiles)。 **注意：当 `spring.profiles.active` 为空时，对应的连接符 `-` 也将不存在，dataId 的拼接格式变成 `${prefix}.${file-extension}`**
  > - `file-exetension` 为配置内容的数据格式，可以通过配置项 `spring.cloud.nacos.config.file-extension` 来配置。目前只支持 `properties` 和 `yaml` 类型。

- 测试，启动Nacos，启动3377，调用 http://localhost:3377/config/info 查看配置信息，调用成功。

- 自带动态刷新，改变Nacos配置文件内容，再调用接口发现自动刷新。







<span style="color:#000000;font-size:14.0pt;font-weight:bold">分类配置</span>

将统一的配置放在 Nacos 远程配置文件 `bootstrap.yaml` 中，将自己的，比如开发环境设置 `spring.profiles.active = dev` 放在自己的 `application.yaml`里。



**Namespace、Group 和 DataId 的关系：**

- 类似Java里面的包名和类名
- 最外层的namespace是可以用于区分部署环境的，Group和DatalD逻辑上区分两个目标对象
- 默认情况：Namespace=public，Group=DEFAULT_GROUP，默认Cluster是DEFAULT
- **Namespace** 主要用来实现隔离。比方说我们现在有三个环境:开发、测试、生产环境，我们就可以创建三个Namespace，不同的Namespace力间是隔离的。
- **Group** 默认是DEFAULT_GROUP，Group可以把不同的微服务划分到同一个分组里面去
- **Service** 就是微服务，一个Service可以包含多个Cluster(集群)，Nacos默认Cluster是DEFAULT，Cluster是对指定微服务的一个虚拟划分。



**DataId 配置方案：** 指定spring.profile.active和配置文件的DatalD来使不同环境下读取不同的配置



**Group 配置方案：** 同样的 DataId 名，不同的组名，但是要在配置文件增加分组属性 `spring.cloud.nacos.config.group = TEST_GROUP`



**Namespace 配置方案：** 新增命名空间，public 默认的命名空间不能删除。要在配置文件增加分组属性 `spring.cloud.nacos.config.namespace = namespaceID`



## 1.3 集群和持久化配置

[官网](https://nacos.io/zh-cn/docs/cluster-mode-quick-start.html)**架构图：**



![](\img\in-post\springcloud\springcloud-alibaba-02.png)



Nacos 采用了集中式存储方式来支持集群化部署，目前只支持 MySQL 的存储。不配置 MySQL 则使用内嵌式数据库 derby。<span style="color:#FF0000">如果不配置 MySQL，则每个 Nacos 节点都会有一个数据库derby，不能实现集中式</span>。

MySQL 的配置查看遇见的坑1.



<span style="color:#000000;font-size:14.0pt;font-weight:bold">Linux 版 Nacos+MqSQL 生产环境配置</span>

需要 1个 Nginx + 3个 nacos 注册中心 + 1个 mysql

[官网下载](https://github.com/alibaba/nacos/releases) nacos Linux版本，并解压。



**步骤：**

- 修改nacos配置文件`conf/application.properties`：增加数据库配置

  ~~~properties
  ### If use MySQL as datasource:
  spring.datasource.platform=mysql
  
  ### Count of DB:
  db.num=1
  
  ### Connect URL of DB:
  db.url.0=jdbc:mysql://127.0.0.1:3306/nacos?characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true&useUnicode=true&useSSL=false&serverTimezone=UTC
  db.user.0=root
  db.password.0=123456
  ~~~

- Linux服务器上集群配置：`conf/cluster.conf`

  ~~~properties
  #it is ip
  192.168.1.150:3333
  192.168.1.150:4444
  192.168.1.150:5555
  ~~~

- 编辑 Nacos 的启动脚本 startup.sh，使它能够接受不同的启动端。集群启动，我们希望可以类似其它软件的shell命令，传递不同的端口号启动不同的nacos实例。命令:`./startup.sh Ep 3333`，表示启动端口号为3333的nacos服务器实例，和上一步的cluster.conf配置的一致。

  ![](\img\in-post\springcloud\springcloud-alibaba-03.png)

- 启动命令：`./startup.sh -p 3333`

- 修改 Nginx 的配置：

  ![](\img\in-post\springcloud\springcloud-alibaba-04.png)

- 使用命令启动 Nacos：`./startup.sh -p 3333`和4444，5555。启动后`ps -ef| grep nacos| grep -v grep |wc -l`可看到启动了三个 Nacos节点。

- 使用刚才的配置文件启动 nginx：`path/sbin/nginx -c /configPath/nginx.conf`

- 自己写的服务提供者，将 YAML 配置文件中的 `spring.cloud.nacos.discovery.server-addr `属性变为 Nginx 的访问路径。

  

  















# *遇见的坑

<span style="color:#0044FF;font-size:15.0pt;font-weight:bold">1）Nacos启动报错</span>

~~~shell
# 默认你已经配置好所有环境变量
1.先mysql创建数据库Nacos，运行nacos/conf下的sql脚本，配置nacos_config数据库
2.在nacos/conf下的application.properties统一修改配置
	  如果端口占用则修改server.port
	  还要配置jdbc连接信息
3.将startup.cmd中的运行模式改为"startup"单例模式
4.Nacos路径不能有中文
5.mysql8+版本解决方法：在你们的nacos目录下面新建/plugins/mysql目录，并把你们8+版本的mysql驱动jar包放到这个目录下面即可
~~~



<span style="color:#0044FF;font-size:15.0pt;font-weight:bold">2）无法创建Bean：requestMappingHandlerAdapter</span>

报错：

~~~shell
Error creating bean with name 'requestMappingHandlerAdapter' defined in
~~~

原因：jackson 依赖未引入或者版本太低

~~~xml
<dependency>
  <groupId>com.fasterxml.jackson.core</groupId>
  <artifactId>jackson-core</artifactId>
  <version>2.9.6</version>
  <type>bundle</type>
</dependency>
~~~

