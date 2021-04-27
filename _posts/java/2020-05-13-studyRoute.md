---
layout:     post
title:      "Java后端学习路线"
subtitle:   "烟雨迷蒙外自有大恐怖"
date:       2020-05-13 12:00:00
author:     "Wangcy"
catalog: false
header-style: text
tags:
  - Java
  - 学习路线
---

> 自己根据网上资料总结起来的Java后端工程师学习路线
>
> **目光如炬，勇往直前**

![](\img\in-post\java\Java-route.png)

# 1 编程基础

## 1.1 Java语言

### 1.1.1 语言基础

基础语法

面向对象

接口

容器

异常

泛型

反射

注解

I/O

~~图形化（Swing）~~

### 1.1.2 JVM虚拟机

类加载机制

字节码执行机制

JVM内存模型

GC垃圾回收

JVM性能监控与故障分析

JVM调优

### 1.1.3 多线程/并发

并发编程基础

线程池

锁

并发容器

原子类

JUC并发工具类



## 1.2 数据结构和算法

### 1.2.1 数据结构

字符串

数组

链表

堆、栈、队列

二叉树

哈希

图

### 1.2.2 算法

排序

查找

贪心

分治

动态规划

回溯

## 1.3 计算机网络

ARP协议

IP协议、ICMP协议

TCP、UDP协议

DNS/HTTP/HTTPS协议

Session/Cookie

## 1.4 数据库

SQL语句的书写

SQL语句的优化

事务、隔离级别

索引

锁

## 1.5 操作系统

进程线程

并发、锁

内存管理和调度

I/O原理

## 1.6 设计模式

单例

工厂

代理

策略

模板方法

观察者

适配器

责任链

建造者

# 2 研发工具

## 2.1 集成开发环境

Eclipse

Intellij IDEA

VSCode

## 2.2 Linux系统

常用命令

shell脚本

## 2.3 代码管理工具

SVN

git

## 2.4 项目管理/构建工具

Maven

Gradle

# 3 后端应用框架

## 3.1 Spring家族框架

**Spring**

IOC

AOP

**SpringMVC**

**SpringBoot**

自动配置

整合Web

整合数据库（事务问题）

整合权限（Shiro、Spring Security）

整合各种框架（Redis、MQ、RPC框架、NIO框架）

## 3.2 服务器软件

**Web服务器**

Nginx

**应用服务器**

Tomcat

Jetty

Undertow

## 3.3 中间件

**缓存**

Redis（5大数据类型，事务，管道，持久化，集群）

**消息队列**

RecketMQ

RabbitMQ

Kafka

**RPC框架**

Dubbo

gRPC

Thrift

Spring Cloud

Netty

## 3.4 数据库框架

**ORM层框架**

Mybatis

Hibernate

JPA

**连接池**

Druid

HikariCP

C3P0

**分库分表**

MyCAT

Sharding-JDBC

Sharding-Sphere

## 3.5 搜索引擎

ElasticSearch

Solr

## 3.6 分布式/微服务

**服务发现/注册**

Eureka

Consul

Zookeeper

Nacos

**网关**

Zuul

Gateway

**服务调用（负载均衡）**

Ribbon

Feign

**熔断/降级**

Hystrix

**配置中心**

Config

Apollo

Nacos

**认证和授权**

Spring Security

OAuth2

SSO单点登录

**分布式事务**（难点）

JTA接口---Atomikos组件

2PC、3PC

XA模式

TCC模式

- tcc-transaction
- ByteTCC
- EasyTransaction
- Seata

SAGA模式

- ServiceComb
- Seata

LCN模式

- tx-lcn

**任务调度**

Quartz

Elastic-Job

**链路追踪和监控**

Ziokin

Sleuth

Skywalking

**日志分析与监控**

ELK

- ElasticSearch
- Logstash
- Kibana

## 3.7 虚拟化/容器化

**容器技术**

Docker

**容器编排技术**

Kubernates

Swarm

# 4 前端应用框架（后端工程师）

## 4.1 基础套餐

**三大件**

HTML

JavaScript

CSS

**基础库**

jQuery

Ajax

## 4.2 模板框架

JSP/JSTL

Thymeleaf

FreeMarker

## 4.3 组件化框架

Node

Vue

React

Angluar

# 5 运维知识

**Web服务器**

Nginx

**应用服务器**

Tomcat

Jetty

Undertow

**CDN加速**

**持续集成/持续部署**

Jenkins

**代码质量检查**

sonar

**日志收集及分析**

ELK

# 成神

徒手撕源码

光脚造轮子

闭着眼睛深度调优

吊打面试官