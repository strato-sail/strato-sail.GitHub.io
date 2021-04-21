---
layout:     post
title:      "Zookeeper 学习"
subtitle:   "对 Zookeeper 的介绍以及学习使用过程"
date:       2021-04-19 12:00:00
author:     "Wangcy"
catalog: false
header-style: text
tags:
  - 分布式
  - zookeeper
---

参考博客：https://blog.csdn.net/qq_41157588/article/details/106737191

# 1 Zookeeper学习

官网地址：https://zookeeper.apache.org/



**工作机制**：

- 是一个基于观察者模式的分布式服务管理框架，负责存储和管理大家都关心的数据
- 用于服务注册和服务发现
- 接受观察者的注册，一旦这些数据的状态发生变化，Zookeeper就将负责通知已经在Zookeeper上注册的那些观察者做出相应的反应。
- Zookeeper = 文件系统 + 通知机制

**特点**：

- Zookeeper：一个领导者（Leader），多个跟随者（Follower）组成的集群。 
- 集群中只要有半数以上节点存活，Zookeeper集群就能正常服务。 （5/3, 4/3）
- 全局数据一致：每个Server保存一份**相同的数据副本**，Client无论连接到哪个Server，数据都是一致的。 
- 更新请求顺序进行，来自同一个Client的更新请求按其发送顺序依次执行。 
- 数据更新原子性，一次数据更新要么成功，要么失败
- 实时性，在一定时间范围内，Client能读到最新数据。（数据同步快）

**数据结构：**

Zookeeper数据结构和**Unix文件系统**很类似，是一种**树状结构**，每个节点成为一个ZNode，每个ZNode默认能**存储1MB数据**，每个ZNode通过**路径唯一标识**。

**应用场景：**

- 统一命名服务：每台服务器ip地址不好对应，相当于网络配置中的 host 配置。也可以多台服务器对应一个ZNode，当一个访问来了，这个ZNode会自动调节访问哪一台服务器。
- 统一配置管理：Hadoop、Kafka等分布式环境的配置文件自动同步工作。或者配置信息写到一个ZNode上，其他客户端监听watch这个ZNode 的数据。
- 统一集群管理：实时掌握每个节点的状态，将节点信息写入ZNode，监听这个ZNode可获取它的实时状态变化。
- 服务器节点动态上下线：服务器上下线的状态动态监控。下线后ZNode自动删除。
- 软负载均衡：软件的负载均衡，在ZNode中记录每台服务器的访问数，让访问数最少的服务器去处理最新的客户端请求。



## 1.1 Zookeeper初始化

**1．安装前准备**

- 安装 Jdk拷贝
- Zookeeper 安装包
- 到 Linux 系统下解压到指定目录

~~~shell
[root@xk1301the002 software]$ tar -zxvf zookeeper-3.4.10.tar.gz -C /opt/module/
~~~

**2．配置修改**

（1）将`/opt/module/zookeeper-3.4.10/conf` 这个路径下的 `zoo_sample.cfg `修改为` zoo.cfg`；

~~~shell
[root@xk1301the002 conf]$ mv zoo_sample.cfg zoo.cfg
~~~

（2）打开 `zoo.cfg `文件，修改 `dataDir `路径：

~~~shell
[root@xk1301the002 zookeeper-3.4.10]$ vim zoo.cfg
~~~

修改如下内容：

`dataDir=/opt/module/zookeeper-3.4.10/zkData`

（3）在`/opt/module/zookeeper-3.4.10/`这个目录上创建` zkData` 文件夹

~~~shell
[root@xk1301the002 zookeeper-3.4.10]$ mkdir zkData
~~~

**3．操作 Zookeeper**

（1）启动 Zookeeper

~~~shell
[root@xk1301the002 zookeeper-3.4.10]$ bin/zkServer.sh start
~~~

（2）查看进程是否启动

~~~shell
[root@xk1301the002 zookeeper-3.4.10]$ jps
4020 Jps
4001 QuorumPeerMain
~~~

（3）查看状态：

~~~shell
[root@xk1301the002 zookeeper-3.4.10]$ bin/zkServer.sh status
ZooKeeper JMX enabled by default
Using config: /opt/module/zookeeper-
3.4.10/bin/../conf/zoo.cfg
Mode: standalone
~~~

（4）启动客户端：

~~~shell
[root@xk1301the002 zookeeper-3.4.10]$ bin/zkCli.sh
~~~

（5）退出客户端：

~~~shell
[zk: localhost:2181(CONNECTED) 0] quit
~~~

（6）停止 Zookeeper

~~~shell
[root@xk1301the002 zookeeper-3.4.10]$ bin/zkServer.sh stop
~~~

**4．配置参数解读**

Zookeeper中的配置文件zoo.cfg中参数含义解读如下： 

1．**`tickTime =2000`：通信心跳数，Zookeeper 服务器与客户端心跳时间，单位毫秒**

Zookeeper使用的基本时间，服务器之间或客户端与服务器之间维持心跳的时间间隔，也就是每个tickTime时间就会发送一个心跳，时间单位为毫秒。

它用于心跳机制，并且设置最小的session超时时间为两倍心跳时间。(session的最小超时时间是2*tickTime)

2．**`initLimit =10`：LF 初始通信时限**

集群中的Follower跟随者服务器与Leader领导者服务器之间初始连接时能容忍的最多心跳数（tickTime的数量），用它来限定集群中的Zookeeper服务器连接到Leader的时限。

3．**`syncLimit =5`：LF 同步通信时限**

集群中Leader与Follower之间的最大响应时间单位，假如响应超过syncLimit * tickTime，Leader认为Follwer死掉，从服务器列表中删除Follwer。 

4．**`dataDir`：数据文件目录+数据持久化路径**

主要用于保存 Zookeeper 中的数据。 

5．**`clientPort =2181`：客户端连接端口**

监听客户端连接的端口。 



## 1.2 Zookeeper内部原理

**1. 选举机制**

选举机制用来选出leader节点。

**半数机制**：集群中半数以上的机器存活，集群可用。

> 每台机器进来先投自己，投自己后集群还是不能启动则投机器号大的，直到半数机制符合。
>
> server1  server2  server3  server4  server5
>
> 1. server1投自己后不满足半数（2个）机制，选举无法完成，server1状态保持为LOOKING
> 2. server1 发现server2 的id 比自己的大，转而投票server2，server2也投自己，不满足半数机制
> 3. server1，server2，server3都投给server3，半数机制满足，server3当选leader，server1和server2更改状态为FOLLOWING，server3更改状态为LEADING
> 4. server4 启动，发起一次选举。服务器1、2、3已经不是LOOKING状态，不会更改选票信息。交换选票信息结果：server3 得 3 票，server4 得 1 票。此时server4 服从大多数，更改选票信息为服务器3，状态为FOLLOWING。
> 5. server5  同服务器 4 一样当following。



**2.节点类型**

持久型（persistent）：客户端和服务器断开连接后，创建的节点不删除

- 持久化目录节点
- 持久化顺序编号目录节点：创建znode时设置了顺序标识，znode名称后会附加一个值，顺序号是一个单调递增的计数器，由父节点维护。

短暂型（ephemeral）：客户端和服务器断开连接后，创建的节点自己删除

- 临时目录节点
- 临时顺序编号目录节点：有顺序编号



**3. Stat结构体**

`state /path` 命令的信息

1）czxid-创建节点的事务 zxid

每次修改 ZooKeeper 状态都会收到一个 zxid 形式的时间戳，也就是 ZooKeeper 事务 ID。事务 ID 是 ZooKeeper 中所有修改总的次序。每个修改都有唯一的 zxid，如果 zxid1 小于 zxid2，那么 zxid1 在 zxid2 之前发生。

2）ctime - znode 被创建的毫秒数(从 1970 年开始) 

3）mzxid - znode 最后更新的事务 zxid

4）mtime - znode 最后修改的毫秒数(从 1970 年开始) 

5）pZxid-znode 最后更新的子节点 zxid

6）cversion - znode 子节点变化号，znode 子节点修改次数

7）dataversion - znode 数据变化号

8）aclVersion - znode 访问控制列表的变化号

9）ephemeralOwner- 如果是临时节点，这个是 znode 拥有者的 session id。如果不是临时节

点则是 0。 

**10）dataLength- znode 的数据长度**

**11）numChildren - znode 子节点数量**



**4. 监听器的原理**

> get /path watch
>
> ls /path watch

- 在`main()`线程中创建 Zookeeper 客户端，这时就会创建两个线程，一个负责网络连接通信（connect），一个负责监听（listener）。
- 通过`connect`线程将注册的监听事件发送给 Zookeeper 。
- 在 Zookeeper 的注册监听器列表中将注册的监听事件添加到列表中。
- Zookeeper 监听到有数据或路径变化，就将这个消息发送给`listener`线程。
- `listerner`线程内部调用了`process()`方法

![](\img\in-post\zookeeper\zookeeper-01.png)



**5. 写数据的流程**

只有 leader 有资格给所有  follower 发写请求的广播。

![](\img\in-post\zookeeper\zookeeper-02.png)





## 1.3 Zookeeper实战

集群至少三台节点

**1. 配置服务器编号**

在`conf/zoo.cfg`的`dataDir`路径中创建一个文件`myid`，在此文件中添加与此server对应的编号。

**2. 配置zoo.cfg文件**

修改dataDir路径为自定义数据存储路径

添加如下配置：

~~~she
##########cluster##########
server.1=hadoop01:2888:3888
server.2=hadoop02:2888:3888
server.3=hadoop03:2888:3888
~~~

> server.**A** = **B** : **C** : **D**
>
> **A** 是一个数字，是 dataDir 下 myid 文件的编号；
>
> **B** 是这个服务器的 ip 地址；
>
> **C** 是这个服务器 Follower 与集群中的 Leader 服务器交换信息的端口；
>
> **D** 是万一集群中的 Leader 服务器挂了，需要一个端口重新进行选举，选出新 Leader ，用此端口。

**3. 客户端命令行操作**

| 命令基本语法     | 功能描述                                                     |
| :--------------- | :----------------------------------------------------------- |
| help             | 显示所有操作命令                                             |
| ls path [watch]  | 查看当前 znode 中所包含的内容                                |
| ls2 path [watch] | 查看当前节点数据并能看到更新次数等数据                       |
| create           | 普通创建<br />-s 含有序列（znode路径可重复，因为自动在路径后加了序号）<br />-e 临时（当前客户端重启或者超时时，该znode消失） |
| get path [watch] | 获得节点的值                                                 |
| set              | 设置节点的具体值                                             |
| stat             | 查看节点状态                                                 |
| delete           | 删除节点                                                     |
| rmr              | 递归删除节点                                                 |

**注意：**创建 create -s 含有序号的节点时，如果原来没有序号节点，序号从0开始依次递增。如果原节点下已有2个节点，则再排序时从2开始，以此类推。



**设置监听节点数据值：**注册一次，有效一次：watch一次后，只监听一次变化。

三台服务器server1，server2，server3。

- 在 server1 启动客户端 zkCli.sh ，执行` create /fruit "apple"`
- 在 server2 启动客户端，执行 `get /fruit watch`
- 在server1 的客户端执行 `set /fruit "banana"`后，server2 客户端收到数据变化的通知



**设置监听路径变化：**也是生效一次

- `ls /fruit watch`
- `create /fruit/apple "apple"`



## 1.4 API 程序中应用

**1. Maven依赖**

~~~xml
<dependency>
	<groupId>org.apache.zookeeper</groupId>
	<artifactId>zookeeper</artifactId>
	<version>3.4.10</version>
</dependency>
~~~

**2. 创建Zookeeper客户端**

构造函数：

~~~java
public ZooKeeper(String connectString, int sessionTimeout, Watcher watcher) 
    throws IOException
{
    this(connectString, sessionTimeout, watcher, false);
}
~~~

应用：

~~~java
String connectString = "hadoop002:2181,hadoop003:2181,hadoop004:2181";
int sessionTimout = 2000;  //ms
ZooKeeper zkClient = new ZooKeeper(connectString, sessionTimout, new Watcher() {
     @Override
     public void process(WatchedEvent watchedEvent) {
	}
});
~~~

**3. 创建子节点**

~~~java
// 参数 1：要创建的节点的路径； 参数 2：节点数据 ； 参数 3：节点权限 ；参数 4：节点的类型
String nodeCreated = zkClient.create("/wcy",
                "wangchengyuan".getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT);
~~~

**4. 获取子节点并监听节点变化**

![](\img\in-post\zookeeper\zookeeper-03.png)

可选择监听的内容。

~~~java
public List<String> getChildren(String path, boolean watch)
            throws KeeperException, InterruptedException {
        return getChildren(path, watch ? watchManager.defaultWatcher : null);
    }
~~~

~~~java
// 获取子节点
@Test
public void getChildren() throws Exception {
	List<String> children = zkClient.getChildren("/", true);
	for (String child : children) {
		System.out.println(child);
	}
	// 延时阻塞
	Thread.sleep(Long.MAX_VALUE);
}
//然后增强for也要放在监听器里，这样控制台能实时打印
~~~

**5. 判断是否存在**

~~~java
Stat stat = zkClient.exists（“/path”, boolean watch）; //true监听
sout(stat == null ? "not exist" : "exist");
~~~



## 1.5 监听节点动态上下线案例

分布式系统中，服务器节点有多台，可以动态上下线，任意一个客户端都能实时感知到主节点服务器的上下线。

**每个服务器都是短暂节点（下线后自动删除 ZNode）**

![](\img\in-post\zookeeper\zookeeper-04.png)

**1. 创建 /server 节点**

~~~shell
[zk: localhost:2181(CONNECTED) 10]$ create /servers "servers"
Created /servers
~~~

**2. 服务器端向Zookeeper注册的代码**

~~~java
package com.example.springbootdemo.zk;

import org.apache.zookeeper.*;

import java.io.IOException;

public class DistributeServer {
    private static String connectString =
            "hadoop102:2181,hadoop103:2181,hadoop104:2181";
    private static int sessionTimeout = 2000;
    private ZooKeeper zk = null;
    private String parentNode = "/servers";

    // 创建到 zk 的客户端连接
    public void getConnect() throws IOException {
        zk = new ZooKeeper(connectString, sessionTimeout, new Watcher() {
                    @Override
                    public void process(WatchedEvent event) {
                    }
                });
    }

    // 注册服务器
    public void registServer(String hostname) throws
            Exception {
        String create = zk.create(parentNode + "/server",
                hostname.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL_SEQUENTIAL);  //-s -e 临时的，带序号的
        System.out.println(hostname + " is online " + create);
    }

    // 业务功能
    public void business(String hostname) throws Exception {
        System.out.println(hostname + " is working ...");
        Thread.sleep(Long.MAX_VALUE);   //保证main不结束
    }

    public static void main(String[] args) throws Exception {
        // 1 获取 zk 连接
        DistributeServer server = new DistributeServer();
        server.getConnect();
        // 2 利用 zk 连接注册服务器信息
        server.registServer(args[0]);
        // 3 启动业务功能
        server.business(args[0]);
    }
}

~~~



**3. 客户端监听服务器上下线的代码**

~~~java
package com.example.springbootdemo.zk;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DistributeClient {
    private static String connectString =
            "hadoop102:2181,hadoop103:2181,hadoop104:2181";
    private static int sessionTimeout = 2000;
    private ZooKeeper zk = null;
    private String parentNode = "/servers";

    // 创建到 zk 的客户端连接
    public void getConnect() throws IOException {
        zk = new ZooKeeper(connectString, sessionTimeout, new Watcher() {
                    @Override
                    public void process(WatchedEvent event) {
                    // 再次启动监听
                        try {
                            getServerList();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    // 获取服务器列表信息
    public void getServerList() throws Exception {
        // 1 获取服务器子节点信息，并且对父节点进行监听
        List<String> children = zk.getChildren(parentNode,
                true);
        // 2 存储服务器信息列表
        ArrayList<String> servers = new ArrayList<>();
        // 3 遍历所有节点，获取节点中的主机名称信息
        for (String child : children) {
            byte[] data = zk.getData(parentNode + "/" + child,
                    false, null);
            servers.add(new String(data));
        }
        // 4 打印服务器列表信息
        System.out.println(servers);
    }

    // 业务功能
    public void business() throws Exception {
        System.out.println("client is working ...");
        Thread.sleep(Long.MAX_VALUE);
    }

    public static void main(String[] args) throws Exception {
        // 1 获取 zk 连接
        DistributeClient client = new DistributeClient();
        client.getConnect();
        // 2 获取 servers 的子节点信息，从中获取服务器信息列表
        client.getServerList();
        // 3 业务进程启动
        client.business();
    }
}
~~~





# 2 Zookeeper在应用中的问题

