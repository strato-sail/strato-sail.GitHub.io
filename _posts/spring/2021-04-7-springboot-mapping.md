---
layout:     post
title:      "SpringBoot请求映射原理"
subtitle:   "Springboot request mapping principle"
date:       2021-04-07 12:00:00
author:     "Wangcy"
catalog: false
header-style: text
tags:
  - spring 
  - springboot
  - 请求映射
---

# 请求映射的原理

 

Springboot中所有的请求过来都会来到DispatcherServlet，springboot底层还是springMVC，springMVC的DispatcherServlet是处理所有请求的开始。

DispatcherServlet继承自HttpServlet，所以必然子类会重写doget和dopost方法。

![继承树](/img/in-post/spring/post-spring-springbootmapping-01.png)

![继承树](/img/in-post/spring/post-spring-springbootmapping-02.png)

在第三步中，HandlerMapping，处理器映射规则，每个url的请求对应哪一个handler处理。这些规则保存在handlerMappings里。

![继承树](/img/in-post/spring/post-spring-springbootmapping-03.png)

运行起来后handlerMappings里有个组件叫RequestMappingHandlerMapping，保存了所有@RequestMapping和handler的映射规则，相当于所有@RequestMapping处理器映射。（应用一启动，spring扫描所有映射注解，将所有注解保存在handlerMapping里）

RequestMappingHandlerMapping里有个组件叫mappingRegistry，映射注册中心，里面存放我们项目中自己写的的所有映射。

第三步中的getHandler相当于遍历系统中的请求映射，看谁能处理当前请求。

![继承树](/img/in-post/spring/post-spring-springbootmapping-04.png)

> **总结**
>
> 所有的请求映射都在HandlerMapping中
>
> 1. SpringBoot自动配置欢迎页的WelcomePageHandlerMapping HandlerMapping，能访问 / 能访问到index.html
> 2. SpringBoot自动配置了默认的RequestMappingHandlerMapping
> 3. 请求进来，挨个尝试错油的HandlerMapping看是否有请求信息
>    - 如果有就找到这个请求对应的Handler
>    - 如果没有就是下一个HafndlerMapping
>
> 4. 我们需要一些自定义的映射处理，我们也可以自己给容器中放HandlerMapping，自定义HandlerMapping



SpringBoot给我们放的HandlerMapping（在WebMvcAutoConfiguration里）：

1.  RequestMappingHandlerMapping：我们当前标@RequestMapping自定义的Controller
2. 欢迎页的WelcomePageHandlerMapping
3.  等等