---
layout:     post
title:      "IDEA 建立多模块工程"
subtitle:   "create muti module project for idea"
date:       2021-04-27 12:00:00
author:     "Wangcy"
catalog: false
header-style: text
tags:
  - IDEA
  - Maven
  - 环境搭建
---



# 搭建Maven父工程

IDEA->create new project->Maven

![](\img\in-post\idea\muti-module-project-01.png)



## **1) 定义`Maven `的`groupId`和`artifactId`：**

一般ArtifactID为工程名。

> GroupID 是项目组织唯一的标识符，实际对应JAVA的包的结构，是main目录里java的目录结构。 
>
>
> ArtifactID是项目的唯一的标识符，实际对应项目的名称，就是项目根目录的名称。 

![](\img\in-post\idea\muti-module-project-02.png)



## **2) File Type过滤**

将一些用不到的文件过滤，不出现在目录书里，使工程看起来清爽。

file->settings

![](\img\in-post\idea\muti-module-project-03.png)



## **3) 对父工程的POM文件整理**

父工程的打包方式 : <packaging>pom</packaging> 表明这是个父工程的pom

删去父工程src，使父工程只留下一个pom文件：

![](\img\in-post\idea\muti-module-project-04.png)



#### dependencyManagement 标签作用：

- 子模块继承之后，提供作用，锁定版本并且子`module`不用写`groupId`和`version`。
- 用来提供管理依赖版本号的方式，**通常会在一个组织或者项目的最顶层的父POM中看到**dependencyManagement元素。
- 使用pom.xml 中的dependencyManagement元素能**让所有在子项目中引用一个依赖而不用显式的列出版本号**。Maven 会沿着父子层次向上走，直到找到一个拥有dependencyManagement元素的项目，然后它就会使用这个dependencyManagement元素中指定的版本号。（双亲委派，先用父类的，父类没有用子类的）
- 这样做的**好处**就是:如果有多个子项目都引用同一样依赖，则可以避免在每个使用的子项目里都声明一个版本号，这样当想升级或切换到另一个版本时，只需要在顶层父容器里更新，而不需要一个一个子项目的修改;另外如果某个子项目需要另外的一个版本，只需要声明version就可。
- dependencyManagement里**只是声明依赖，并不实现引入**，因此子项目需要显示的声明需要用的依赖
- 如果不在子项目中声明依赖，是不会从父项目中继承下来的；只有在子项目中写了该依赖项，并且没有指定具体版本,才会从父项目中继承该项，并且version和scope都读取自父pom。
- 如果子项目中指定了版本号，那么**会使用子项目中指定的**jar版本。



父工程的POM模板：

~~~xml
<?xml version="1.0" encoding="UTF-8"?>

<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>com.wcy.springcloud</groupId>
  <artifactId>cloudstudy</artifactId>
  <version>1.0-SNAPSHOT</version>
  <packaging>pom</packaging>

    <modules>
        <module>cloud-provider-payment8001</module>
    </modules>


  <!-- 统一管理jar包版本 -->
  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <maven.compiler.source>18</maven.compiler.source>
    <maven.compiler.target>18</maven.compiler.target>
    <junit.version>4.12</junit.version>
    <lombok.version>1.18.10</lombok.version>
    <log4j.version>1.2.17</log4j.version>
    <mysql.version>5.1.47</mysql.version>
    <druid.version>1.1.16</druid.version>
    <mybatis.spring.boot.version>2.1.3</mybatis.spring.boot.version>
  </properties>

  <!--子模块继承之后，提供作用：锁定版本+子module不用写groupId和version-->
  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-project-info-reports-plugin</artifactId>
        <version>3.0.0</version>
      </dependency>
      <!--spring boot 2.2.2-->
      <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-dependencies</artifactId>
        <version>2.2.2.RELEASE</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
      <!--spring cloud Hoxton.SR1-->
      <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-dependencies</artifactId>
        <version>Hoxton.SR1</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
      <!--spring cloud 阿里巴巴-->
      <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-alibaba-dependencies</artifactId>
        <version>2.1.0.RELEASE</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
      <!--mysql-->
      <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>${mysql.version}</version>
        <scope>runtime</scope>
      </dependency>
      <!-- druid-->
      <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>druid</artifactId>
        <version>${druid.version}</version>
      </dependency>
        <!--mybatis-->
      <dependency>
        <groupId>org.mybatis.spring.boot</groupId>
        <artifactId>mybatis-spring-boot-starter</artifactId>
        <version>${mybatis.spring.boot.version}</version>
      </dependency>
      <!--junit-->
      <dependency>
        <groupId>junit</groupId>
        <artifactId>junit</artifactId>
        <version>${junit.version}</version>
      </dependency>
      <!--log4j-->
      <dependency>
        <groupId>log4j</groupId>
        <artifactId>log4j</artifactId>
        <version>${log4j.version}</version>
      </dependency>
    </dependencies>

  </dependencyManagement>

  <!--bulid是这样的用springboot默认的build方式-->
  <build>
    <plugins>
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
        <configuration>
          <fork>true</fork>
          <addResources>true</addResources>
        </configuration>
      </plugin>
    </plugins>
  </build>

</project>

~~~



# 子Module搭建

**步骤：**

1. 建Module
2. 改POM
3. 写YML
4. 主启动
5. 业务类

## **1) 步骤1：建Module**

![](\img\in-post\idea\muti-module-project-05.png)

使用Maven：

![](\img\in-post\idea\muti-module-project-06.png)

继承父工程：

![](\img\in-post\idea\muti-module-project-07.png)

重写新module名字：

![](\img\in-post\idea\muti-module-project-08.png)

父工程的pom文件里会多一个mudules标签，表明子模块：

![](\img\in-post\idea\muti-module-project-09.png)

## **2) 步骤2：改POM**

子pom文件：

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

    <artifactId>cloud-provider-payment8001</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.mybatis.spring.boot</groupId>
            <artifactId>mybatis-spring-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid-spring-boot-starter</artifactId>
            <version>1.1.10</version>
        </dependency>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-jdbc</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
        </dependency>
    </dependencies>

</project>
~~~

## **3) 步骤3：写YML**

![](\img\in-post\idea\muti-module-project-10.png)

## **4) 步骤4：写主启动类**

![](\img\in-post\idea\muti-module-project-11.png)

## **5) 步骤5：业务类**