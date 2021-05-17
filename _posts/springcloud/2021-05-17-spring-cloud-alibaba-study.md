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









# 简介

<span style="color:#0044FF;font-size:15.0pt;font-weight:bold">2）单机版的 eureka</span>



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



# Nacos 服务注册和配置中心

SpringCloud Alibaba Nacos

