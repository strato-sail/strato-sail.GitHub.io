---
layout:     post
title:      "了解非关系型数据库 NoSQL - MongoDB"
subtitle:   "Introduction to MongoDB and CRUD operations with mongoose"
date:       2021-05-17 12:00:00
author:     "Wangcy"
catalog: false
header-style: text
tags:
  - MongoDB
  - NoSQL
---


# 安装使用以及 CRUD 操作

> 配套资料: `https://pan.baidu.com/s/18au42FIhSNrXY9p7MbmNbg` 提取码: `29ad`
> 
> 感谢 B 站用户 [`冷鸟丨会飞 `](https://space.bilibili.com/55263887) 分享



**课程目标**

MongoDB的副本集: 操作, 主要概念, 故障转移, 选举规则 MongoDB的分片集群：概念, 优点, 操作, 分片策略, 故障转移 MongoDB的安全认证

- 理解 MongoDB 的业务场景, 熟悉 MongoDB 的简介, 特点和体系结构, 数据类型等.
- 能够在 Windows 和 Linux 下安装和启动 MongoDB, 图形化管理界面 Compass 的安装使用
- 掌握 MongoDB 基本常用命令实现数据的 CRUD
- 掌握 MongoDB 的索引类型, 索引管理, 执行计划

## 1. MongoDB 相关概念



<span style="color:#0000FF;font-size:16.0pt;font-weight:bold">1.1 业务场景</span>



传统的关系型数据库 (比如 MySQL), 在数据操作的"三高"需求以及对应的 Web 2.0 网站需求面前, 会有"力不从心"的感觉

所谓的三高需求:

**高并发, 高性能, 高可用**, 简称三高

- High Performance: 对<u>数据库的高并发读写</u>的要求
- High Storage: 对<u>海量数据的高效率存储和访问</u>的需求
- High Scalability && High Available: 对数据的<u>高扩展性和高可用性</u>的需求

**而 MongoDB 可以应对三高需求**

具体的应用场景:

- 社交场景, 使用 MongoDB 存储存储用户信息, 以及用户发表的朋友圈信息, 通过地理位置索引实现附近的人, 地点等功能.
- 游戏场景, 使用 MongoDB 存储游戏用户信息, 用户的装备, 积分等直接以内嵌文档的形式存储, 方便查询, 高效率存储和访问.
- 物流场景, 使用 MongoDB 存储订单信息, 订单状态在运送过程中会不断更新, 以 MongoDB 内嵌数组的形式来存储, 一次查询就能将订单所有的变更读取出来.
- 物联网场景, 使用 MongoDB 存储所有接入的智能设备信息, 以及设备汇报的日志信息, 并对这些信息进行多维度的分析.
- 视频直播, 使用 MongoDB 存储用户信息, 点赞互动信息等.


这些应用场景中, 数据操作方面的共同点有:

1. 数据量大
2. 写入操作频繁
3. 价值较低的数据, 对**事务性**要求不高

对于这样的数据, 更适合用 MongoDB 来实现数据存储


那么我们**什么时候选择 MongoDB 呢?**

除了架构选型上, 除了上述三个特点之外, 还要考虑下面这些问题:

- 应用不需要事务及复杂 JOIN 支持
- 新应用, 需求会变, 数据模型无法确定, 想快速迭代开发
- 应用需要 2000 - 3000 以上的读写QPS（更高也可以）
- 应用需要 TB 甚至 PB 级别数据存储
- 应用发展迅速, 需要能快速水平扩展
- 应用要求存储的数据不丢失
- 应用需要 `99.999%` 高可用
- 应用需要大量的地理位置查询, 文本查询

如果上述有1个符合, 可以考虑 MongoDB, 2个及以上的符合, 选择 MongoDB 绝不会后悔.


> 如果用MySQL呢?
>
> 相对MySQL, 可以以更低的成本解决问题（包括学习, 开发, 运维等成本）



<span style="color:#0000FF;font-size:16.0pt;font-weight:bold">1.2 MongoDB 简介</span>



> MongoDB是一个开源, 高性能, 无模式的文档型数据库, 当初的设计就是用于简化开发和方便扩展, 是NoSQL数据库产品中的一种.是**最像关系型数据库（MySQL）的非关系型数据库.** 
>
> 它支持的数据结构非常松散, 是一种类似于 JSON 的 格式叫BSON(二进制的 JSON), 所以它既可以存储比较复杂的数据类型, 又相当的灵活. 
>
> **MongoDB中的记录是一个文档,** 它是一个由字段和值对（ﬁeld:value）组成的数据构.MongoDB文档类似于JSON对象, 即一个文档认 为就是一个对象.字段的数据类型是字符型, 它的值除了使用基本的一些类型外, 还可以包括其他文档, 普通数组和文档数组.



**"最像关系型数据库的 NoSQL 数据库"**. MongoDB 中的记录是一个文档, 是一个 key-value pair. 字段的数据类型是字符型, 值除了使用基本的一些类型以外, 还包括其它文档, 普通数组以及文档数组


![](F:\strato-sail\strato-sail.GitHub.io\img\in-post\mongodb\mongodb-01.png)


![](F:\strato-sail\strato-sail.GitHub.io\img\in-post\mongodb\mongodb-02.png)


MongoDB 数据模型是面向<u>文档</u>的, 所谓文档就是一种类似于 JSON 的结构, 简单理解 MongoDB 这个数据库中存在的是各种各样的 JSON（BSON）


- 数据库 (database)
  - 数据库是一个仓库, 存储集合 (collection)
- 集合 (collection)
  - 类似于数组, 在集合中存放文档
- 文档 (document)
  - 文档型数据库的最小单位, 通常情况, 我们存储和操作的内容都是文档



在 MongoDB 中, 数据库和集合都不需要手动创建, 当我们创建文档时, 如果文档所在的集合或者数据库不存在, **则会自动创建数据库或者集合**



<span style="color:#000000;font-size:14.0pt;font-weight:bold">数据库 (databases) 管理语法</span>

| 操作                                            | 语法                             |
| ----------------------------------------------- | -------------------------------- |
| 查看所有数据库                                  | `show dbs;` 或 `show databases;` |
| 查看当前数据库                                  | `db;`                            |
| 切换到某数据库 (**若数据库不存在则创建数据库**) | `use <db_name>;`                 |
| 删除当前数据库                                  | `db.dropDatabase();`             |



<span style="color:#000000;font-size:14.0pt;font-weight:bold">集合 (collection) 管理语法</span>

| 操作         | 语法                                        |
| ------------ | ------------------------------------------- |
| 查看所有集合 | `show collections;`                         |
| 创建集合     | `db.createCollection("<collection_name>");` |
| 删除集合     | `db.<collection_name>.drop()`               |



<span style="color:#0000FF;font-size:16.0pt;font-weight:bold">1.3. 数据模型</span>

![](F:\strato-sail\strato-sail.GitHub.io\img\in-post\mongodb\mongodb-03.png)



<span style="color:#0000FF;font-size:16.0pt;font-weight:bold">1.4 MongoDB 的特点</span>

<span style="color:#000000;font-size:14.0pt;font-weight:bold">1.4.1 高性能</span>

MongoDB 提供高性能的数据持久化

- 嵌入式数据模型的支持<u>减少了数据库系统上的 I/O 活动</u>
- 索引支持更快的查询, 并且可以包含来自嵌入式文档和数组的键 (文本索引解决搜索的需求, TTL 索引解决历史数据自动过期的需求, 地理位置索引可以用于构件各种 O2O 应用)
- mmapv1, wiredtiger, mongorocks (rocksdb) in-memory 等多引擎支持满足各种场景需求
- Gridfs 解决文件存储需求

<span style="color:#000000;font-size:14.0pt;font-weight:bold">1.4.2 高可用</span>

MongoDB 的复制工具称作**副本集** (replica set) 可以提供自动<u>故障转移和数据冗余</u>

<span style="color:#000000;font-size:14.0pt;font-weight:bold">1.4.3 高扩展</span>

水平扩展是其核心功能一部分

分片将数据分布在一组集群的机器上 (海量数据存储, 服务能力水平扩展)

MongoDB 支持基于**片键**创建数据区域, 在一个平衡的集群当中, MongoDB 将一个区域所覆盖的读写**只定向**到该区域的那些片

<span style="color:#000000;font-size:14.0pt;font-weight:bold">1.4.4 其他</span>

MongoDB支持丰富的查询语言, 支持读和写操作(CRUD), 比如数据聚合, 文本搜索和地理空间查询等. 无模式（动态模式）, 灵活的文档模型



## 2. 单机部署

<span style="color:#0000FF;font-size:16.0pt;font-weight:bold">2.1 Windows系统中的安装启动</span>

- 去[官网](https://www.mongodb.com/download-center#community)下载 zip 版本

- 将压缩包解压到一个目录中，在解压目录中手动建立一个目录`\data\db`用于存放数据文件

- 启动方式1：在`bin`目录中输入命令：

  ~~~shell
  mongod --dbpath=..\data\db
  ~~~

  在启动信息可以看到，默认端口号未27017，如果想指定启动端口，可以通过`--port`来指定。

- 启动方式2：配置文件方式

  在解压目录下建立文件夹`\conf\mongod.conf`用于存放配置文件（见坑1），

  ```yaml
  systemLog:
       destination: file 
       #The path of the log file to which mongod or mongos should send all diagnostic logging information 
       path: "D:/02_Server/DBServer/mongodb-win32-x86_64-2008plus-ssl-4.0.1/log/mongod.log"
       logAppend: true
  storage: 
  	journal: 
  		enabled: true 
  	#The directory where the mongod instance stores its data.Default Value is "/data/db". 
  	dbPath: "D:/02_Server/DBServer/mongodb-win32-x86_64-2008plus-ssl-4.0.1/data" 
  net:
  	#bindIp: 127.0.0.1 
  	port: 27017 
  setParameter: 
  	enableLocalhostAuthBypass: false
  ```

  命令：

  ~~~shell
  mongod -f ../config/mongod.conf 
  或
  mongod --config ../config/mongod.conf
  ~~~

- 输入以下mongo 命令完成登录：

  ~~~shell
  mongo
  或
  mongo --host=127.0.0.1 --port=27017
  ~~~



<span style="color:#0000FF;font-size:16.0pt;font-weight:bold">2.2 Compass 图形化界面客户端</span>

到MongoDB官网下载MongoDB Compass，

地址：https://www.mongodb.com/download-center/v2/compass?initial=true

如果是下载安装版，则按照步骤安装；如果是下载加压缩版，直接解压，执行里面的 MongoDBCompassCommunity.exe 文件即可。

在打开的界面中，输入主机地址、端口等相关信息，点击连接



<span style="color:#0000FF;font-size:16.0pt;font-weight:bold">2.3 Linux系统的安装</span>

- 下载压缩包

- 解压`tar -xvf mongodb-linux-x86_64-4.0.10.tgz `

- 新建目录，用来存储数据和日志

  ~~~shell
  #数据存储目录
  mkdir -p /mongodb/single/data/db
  #日志存储目录 
  mkdir -p /mongodb/single/log
  ~~~

- 新建并修改配置文件`vi /mongodb/single/mongod.conf `

  ~~~yaml
  systemLog:
  	#MongoDB发送所有日志输出的目标指定为文件
  	# #The path of the log file to which mongod or mongos should send al1 diagnostic logging 
  	informationdestination: file
  	#mongod或mongos应向其发送所有诊断日志记录信息的日志文件的路径
  	path : " /mongodb/single/log/mongod . 1og"
  	#当mongos或mongod实例重新启动时，mongos或mongod会将新条目附加到现有日志文件的末尾。
  	logAppend: true
  storage:
  	#mongod实例存储其数据的目录。storage. dbPath设置仅适用于mongod。
  	##The directory where the mongod instance stores its data.Default value is "/data/db".
  	dbPath: " /mongodb/single/data/db"
  	journal:
  	#启用或禁用持久性日志以确保数据文件保持有效和可恢复。
  		enabled: true
  processManagement:
  	#启用在后台运行mongos或mongod进程的守护进程模式。
  	fork : true
  net:
  	#服务实例绑定的IP，默认是1ocalhost
  	bindIp: localhost,192.168.0.2
  	#bindIp #绑定的端口，默认是27017 
  	port: 27017
  ~~~

- 启动MongoDB服务





## 3. 基本常用命令



<span style="color:#0000FF;font-size:16.0pt;font-weight:bold">2.1 数据库操作</span>

默认保留的数据库

- **admin**: 从权限角度考虑, 这是 `root` 数据库, 如果将一个用户添加到这个数据库, 这个用户自动继承所有数据库的权限, 一些特定的服务器端命令也只能从这个数据库运行, 比如列出所有的数据库或者关闭服务器
- **local**: 数据永远不会被复制, 可以用来存储限于本地的单台服务器的集合 (部署集群, 分片等)
- **config**: Mongo 用于分片设置时, `config` 数据库在内部使用, 用来保存分片的相关信息



>```sh
>$ show dbs
>$ use articledb
>$ db.dropDatabase()
>```
>
>use 命令，数据库不存在则自动创建
>
>db.dropDatabase() 用于删除
>
>当使用 `use articledb` 的时候. `articledb` 其实存放在内存之中, 当 `articledb` 中存在一个 collection 之后, mongo 才会将这个数据库持久化到硬盘之中.



<span style="color:#0000FF;font-size:16.0pt;font-weight:bold">2.2 集合隐式创建</span>

**显式创建**基本语法格式：

~~~shell
db.createCollection(name)
~~~

**隐式创建**：向一个集合插入一个文档的时候，如果集合不存在，则会自动创建集合。（通常我们使用隐式创建文档即可）

~~~shell
show collections   # 列出集合
db.collection.drop()   # 删除集合
~~~



<span style="color:#0000FF;font-size:16.0pt;font-weight:bold">2.3 文档基本 CRUD</span>


> 官方文档: https://docs.mongodb.com/manual/crud/

<span style="color:#000000;font-size:14.0pt;font-weight:bold">2.3.1 创建 Create</span>

> Create or insert operations add new [documents](https://docs.mongodb.com/manual/core/document/#bson-document-format) to a [collection](https://docs.mongodb.com/manual/core/databases-and-collections/#collections). If the collection does **not** currently exist, insert operations will create the collection automatically.



- 使用 `db.<collection_name>.insertOne()` 向集合中添加*一个文档*, 参数一个 json 格式的文档
- 使用 `db.<collection_name>.insertMany()` 向集合中添加*多个文档*, 参数为 json 文档数组

<img src="https://docs.mongodb.com/manual/_images/crud-annotated-mongodb-insertOne.bakedsvg.svg" style="zoom:67%;" />



```javascript
db.collection.insert({
  <document or array of documents>,
  writeConcern: <document>,
  ordered: <boolean>
})


// 向集合中添加一个文档
db.collection.insertOne(
   { item: "canvas", qty: 100, tags: ["cotton"], size: { h: 28, w: 35.5, uom: "cm" } }
)
// 向集合中添加多个文档
db.collection.insertMany([
   { item: "journal", qty: 25, tags: ["blank", "red"], size: { h: 14, w: 21, uom: "cm" } },
   { item: "mat", qty: 85, tags: ["gray"], size: { h: 27.9, w: 35.5, uom: "cm" } },
   { item: "mousepad", qty: 25, tags: ["gel", "blue"], size: { h: 19, w: 22.85, uom: "cm" } }
])
```



注：当我们向 `collection` 中插入 `document` 文档时, 如果没有给文档指定 `_id` 属性, 那么数据库会为文档自动添加 `_id` field, 并且值类型是 `ObjectId(blablabla)`, 就是文档的唯一标识, 类似于 relational database 里的 `primary key`



> - mongo 中的数字, 默认情况下是 double 类型, 如果要存整型, 必须使用函数 `NumberInt(整型数字)`, 否则取出来就有问题了
> - 插入当前日期可以使用 `new Date()`



如果某条数据插入失败, 将会终止插入, 但已经插入成功的数据**不会回滚掉**. 因为批量插入由于数据较多容易出现失败, 因此, 可以使用 `try catch` 进行异常捕捉处理, 测试的时候可以不处理.如：



```javascript
try {
  db.comment.insertMany([
    {"_id":"1","articleid":"100001","content":"我们不应该把清晨浪费在手机上, 健康很重要, 一杯温水幸福你我 他.","userid":"1002","nickname":"相忘于江湖","createdatetime":new Date("2019-0805T22:08:15.522Z"),"likenum":NumberInt(1000),"state":"1"},
    {"_id":"2","articleid":"100001","content":"我夏天空腹喝凉开水, 冬天喝温开水","userid":"1005","nickname":"伊人憔 悴","createdatetime":new Date("2019-08-05T23:58:51.485Z"),"likenum":NumberInt(888),"state":"1"},
    {"_id":"3","articleid":"100001","content":"我一直喝凉开水, 冬天夏天都喝.","userid":"1004","nickname":"杰克船 长","createdatetime":new Date("2019-08-06T01:05:06.321Z"),"likenum":NumberInt(666),"state":"1"},
    {"_id":"4","articleid":"100001","content":"专家说不能空腹吃饭, 影响健康.","userid":"1003","nickname":"凯 撒","createdatetime":new Date("2019-08-06T08:18:35.288Z"),"likenum":NumberInt(2000),"state":"1"},
    {"_id":"5","articleid":"100001","content":"研究表明, 刚烧开的水千万不能喝, 因为烫 嘴.","userid":"1003","nickname":"凯撒","createdatetime":new Date("2019-0806T11:01:02.521Z"),"likenum":NumberInt(3000),"state":"1"}

]);

} catch (e) {
  print (e);
}
```



<span style="color:#000000;font-size:14.0pt;font-weight:bold">2.3.2 查询 Read</span>



- 使用 `db.<collection_name>.find()` 方法对集合进行查询, 接受一个 json 格式的查询条件. 返回的是一个**数组**
- `db.<collection_name>.findOne()` 查询集合中符合条件的<u>第一个</u>文档, 返回的是一个**对象**


![](F:\strato-sail\strato-sail.GitHub.io\img\in-post\mongodb\mongodb-04.png)


可以使用 `$in` 操作符表示*范围查询*

```javascript
db.inventory.find( { status: { $in: [ "A", "D" ] } } )
```



多个查询条件用逗号分隔, 表示 `AND` 的关系

```javascript
db.inventory.find( { status: "A", qty: { $lt: 30 } } )
```



等价于下面 sql 语句

```mysql
SELECT * FROM inventory WHERE status = "A" AND qty < 30
```



使用 `$or` 操作符表示后边数组中的条件是OR的关系

```javascript
db.inventory.find( { $or: [ { status: "A" }, { qty: { $lt: 30 } } ] } )
```



等价于下面 sql 语句

```mysql
SELECT * FROM inventory WHERE status = "A" OR qty < 30
```



联合使用 `AND` 和 `OR` 的查询语句

```javascript
db.inventory.find( {
     status: "A",
     $or: [ { qty: { $lt: 30 } }, { item: /^p/ } ]
} )
```



在 terminal 中查看结果可能不是很方便, 所以我们可以用 `pretty()` 来帮助阅读

```javascript
db.inventory.find().pretty()
```



匹配内容

```javascript
db.posts.find({
  comments: {
    $elemMatch: {
      user: 'Harry Potter'
    }
  }
}).pretty()

// 正则表达式
db.<collection_name>.find({ content : /once/ })
```



创建索引

```javascript
db.posts.createIndex({
  { title : 'text' }
})

// 文本搜索
// will return document with title "Post One"
// if there is no more posts created
db.posts.find({
  $text : {
    $search : "\"Post O\""
  }
}).pretty()
```



<span style="color:#000000;font-size:14.0pt;font-weight:bold">2.3.3 更新 Update</span>



- 使用 `db.<collection_name>.updateOne(<filter>, <update>, <options>)` 方法修改一个匹配 `<filter>` 条件的文档（默认）
- 使用 `db.<collection_name>.updateMany(<filter>, <update>, <options>)` 方法修改所有匹配 `<filter>` 条件的文档
- 使用 `db.<collection_name>.replaceOne(<filter>, <update>, <options>)` 方法**替换**一个匹配 `<filter>` 条件的文档
- `db.<collection_name>.update(查询对象, 新对象)` 默认情况下会使用<u>新对象替换旧对象</u>



其中 `<filter>` 参数与查询方法中的条件参数用法一致.

如果需要修改指定的属性（局部修改）, 而不是替换需要用“修改操作符”来进行修改（覆盖修改）

- `$set` 修改文档中的制定属性



其中最常用的修改操作符即为`$set`和`$unset`,分别表示**赋值**和**取消赋值**.

```javascript
db.inventory.updateOne(
    { item: "paper" },
    {
        $set: { "size.uom": "cm", status: "P" },
        $currentDate: { lastModified: true }
    }
)

db.inventory.updateMany(
    { qty: { $lt: 50 } },
    {
        $set: { "size.uom": "in", status: "P" },
        $currentDate: { lastModified: true }
    }
)
```

> - uses the [`$set`](https://docs.mongodb.com/manual/reference/operator/update/set/#up._S_set) operator to update the value of the `size.uom` field to `"cm"` and the value of the `status` field to `"P"`,
> - uses the [`$currentDate`](https://docs.mongodb.com/manual/reference/operator/update/currentDate/#up._S_currentDate) operator to update the value of the `lastModified` field to the current date. If `lastModified` field does not exist, [`$currentDate`](https://docs.mongodb.com/manual/reference/operator/update/currentDate/#up._S_currentDate) will create the field. See [`$currentDate`](https://docs.mongodb.com/manual/reference/operator/update/currentDate/#up._S_currentDate) for details.



`db.<collection_name>.replaceOne()` 方法替换除 `_id` 属性外的**所有属性**, 其`<update>`参数应为一个**全新的文档**.

```
db.inventory.replaceOne(
    { item: "paper" },
    { item: "paper", instock: [ { warehouse: "A", qty: 60 }, { warehouse: "B", qty: 40 } ] }
)
```



**批量修改**



```javascript
// 默认会修改第一条
db.document.update({ userid: "30"}, { $set：{username: "guest"} })

// 修改所有符合条件的数据
db.document.update( { userid: "30"}, { $set：{username: "guest"} } , {multi: true} )
```



**列值增长的修改**



如果我们想实现对某列值在原有值的基础上进行增加或减少, 可以使用 `$inc` 运算符来实现

```javascript
db.document.update({ _id: "3", {$inc: {likeNum: NumberInt(1)}} })
```



<span style="color:#000000;font-size:14.0pt;font-weight:bold">修改操作符</span>



| Name                                                         | Description                                                  |
| :----------------------------------------------------------- | :----------------------------------------------------------- |
| [`$currentDate`](https://docs.mongodb.com/manual/reference/operator/update/currentDate/#up._S_currentDate) | Sets the value of a field to current date, either as a Date or a Timestamp. |
| [`$inc`](https://docs.mongodb.com/manual/reference/operator/update/inc/#up._S_inc) | Increments the value of the field by the specified amount.   |
| [`$min`](https://docs.mongodb.com/manual/reference/operator/update/min/#up._S_min) | Only updates the field if the specified value is less than the existing field value. |
| [`$max`](https://docs.mongodb.com/manual/reference/operator/update/max/#up._S_max) | Only updates the field if the specified value is greater than the existing field value. |
| [`$mul`](https://docs.mongodb.com/manual/reference/operator/update/mul/#up._S_mul) | Multiplies the value of the field by the specified amount.   |
| [`$rename`](https://docs.mongodb.com/manual/reference/operator/update/rename/#up._S_rename) | Renames a field.                                             |
| [`$set`](https://docs.mongodb.com/manual/reference/operator/update/set/#up._S_set) | Sets the value of a field in a document.                     |
| [`$setOnInsert`](https://docs.mongodb.com/manual/reference/operator/update/setOnInsert/#up._S_setOnInsert) | Sets the value of a field if an update results in an insert of a document. Has no effect on update operations that modify existing documents. |
| [`$unset`](https://docs.mongodb.com/manual/reference/operator/update/unset/#up._S_unset) | Removes the specified field from a document.                 |



<span style="color:#000000;font-size:14.0pt;font-weight:bold">2.3.4 删除 Delete</span>



- 使用 `db.collection.deleteMany()` 方法删除<u>所有</u>匹配的文档.
- 使用 `db.collection.deleteOne()` 方法删除<u>单个</u>匹配的文档.
- `db.collection.drop()`
- `db.dropDatabase()`



```javascript
db.inventory.deleteMany( { qty : { $lt : 50 } } )
```



> Delete operations **do not drop indexes**, even if deleting all documents from a collection.
>
> 
>
> 一般数据库中的数据都不会真正意义上的删除, 会添加一个字段, 用来表示这个数据是否被删除



<span style="color:#0000FF;font-size:16.0pt;font-weight:bold">2.4 文档排序和投影 (sort & projection)</span>



<span style="color:#000000;font-size:14.0pt;font-weight:bold">2.4.1 排序 Sort</span>

查第三条和第四条：

~~~shell
db.<collection_name>.find().limit(2).skip(2)
~~~

在查询文档内容的时候, 默认是按照 `_id` 进行排序

我们可以用 `$sort` 更改文档排序规则

```
{ $sort: { <field1>: <sort order>, <field2>: <sort order> ... } }
```

For the field or fields to sort by, set the sort order to `1` or `-1` to specify an *ascending* or *descending* sort respectively, as in the following example:

```javascript
db.users.aggregate(
   [
     { $sort : { age : -1, posts: 1 } }
     // ascending on posts and descending on age
   ]
)
```



<span style="color:#000000;font-size:14.0pt;font-weight:bold">`$sort` Operator and Memory</span>

<span style="color:#000000;font-size:14.0pt;font-weight:bold">`$sort` + `$limit` Memory Optimization</span>



When a [`$sort`](https://docs.mongodb.com/manual/reference/operator/aggregation/sort/index.html#pipe._S_sort) precedes a [`$limit`](https://docs.mongodb.com/manual/reference/operator/aggregation/limit/#pipe._S_limit) and there are no intervening stages that modify the number of documents, the optimizer can coalesce the [`$limit`](https://docs.mongodb.com/manual/reference/operator/aggregation/limit/#pipe._S_limit) into the [`$sort`](https://docs.mongodb.com/manual/reference/operator/aggregation/sort/index.html#pipe._S_sort). This allows the [`$sort`](https://docs.mongodb.com/manual/reference/operator/aggregation/sort/index.html#pipe._S_sort) operation to **only maintain the top `n` results as it progresses**, where `n` is the specified limit, and ensures that MongoDB only needs to store `n` items in memory. This optimization still applies when `allowDiskUse` is `true` and the `n` items exceed the [aggregation memory limit](https://docs.mongodb.com/manual/core/aggregation-pipeline-limits/#agg-memory-restrictions).

Optimizations are subject to change between releases.

> 有点类似于用 heap 做 topK 这种问题, 只维护 k 个大小的 heap, 会加速 process



举个栗子:

```javascript
db.posts.find().sort({ title : -1 }).limit(2).pretty()
```



<span style="color:#000000;font-size:14.0pt;font-weight:bold">2.4.2 投影 Projection</span>



有些情况, 我们对文档进行查询并不是需要所有的字段, 比如只需要 id 或者 用户名, 我们可以对文档进行“投影”

- `1` - display
- `0` - dont display

```shell
> db.users.find( {}, {username: 1} )

> db.users.find( {}, {age: 1, _id: 0} )
```



<span style="color:#0000FF;font-size:16.0pt;font-weight:bold">2.5 forEach()</span>

```shell
> db.posts.find().forEach(fucntion(doc) { print('Blog Post: ' + doc.title) })
```



<span style="color:#0000FF;font-size:16.0pt;font-weight:bold">2.6 其他查询方式</span>



<span style="color:#000000;font-size:14.0pt;font-weight:bold">2.6.1 正则表达式</span>



```sh
$ db.collection.find({field:/正则表达式/})

$ db.collection.find({字段:/正则表达式/})
```



<span style="color:#000000;font-size:14.0pt;font-weight:bold">2.6.2 比较查询</span>



`<`, `<=`, `>`, `>=` 这些操作符也是很常用的, 格式如下:

```javascript
db.collection.find({ "field" : { $gt: value }}) // 大于: field > value
db.collection.find({ "field" : { $lt: value }}) // 小于: field < value
db.collection.find({ "field" : { $gte: value }}) // 大于等于: field >= value
db.collection.find({ "field" : { $lte: value }}) // 小于等于: field <= value
db.collection.find({ "field" : { $ne: value }}) // 不等于: field != value
```



<span style="color:#000000;font-size:14.0pt;font-weight:bold">2.6.3 包含查询</span>



包含使用 `$in` 操作符. 示例：查询评论的集合中 `userid` 字段包含 `1003` 或 `1004`的文档

```
db.comment.find({userid:{$in:["1003","1004"]}})
```



不包含使用 `$nin` 操作符. 示例：查询评论集合中 `userid` 字段不包含 `1003` 和 `1004` 的文档

```
db.comment.find({userid:{$nin:["1003","1004"]}})
```



<span style="color:#0000FF;font-size:16.0pt;font-weight:bold">2.7 常用命令小结</span>



```
选择切换数据库：use articledb
插入数据：db.comment.insert({bson数据})
查询所有数据：db.comment.find();
条件查询数据：db.comment.find({条件})
查询符合条件的第一条记录：db.comment.findOne({条件})
查询符合条件的前几条记录：db.comment.find({条件}).limit(条数)
查询符合条件的跳过的记录：db.comment.find({条件}).skip(条数)

修改数据：db.comment.update({条件},{修改后的数据})
        或
        db.comment.update({条件},{$set:{要修改部分的字段:数据})

修改数据并自增某字段值：db.comment.update({条件},{$inc:{自增的字段:步进值}})

删除数据：db.comment.remove({条件})
统计查询：db.comment.count({条件})
模糊查询：db.comment.find({字段名:/正则表达式/})
条件比较运算：db.comment.find({字段名:{$gt:值}})
包含查询：db.comment.find({字段名:{$in:[值1, 值2]}})
        或
        db.comment.find({字段名:{$nin:[值1, 值2]}})

条件连接查询：db.comment.find({$and:[{条件1},{条件2}]})
           或
           db.comment.find({$or:[{条件1},{条件2}]})
```



## 3. 文档间的对应关系

- 一对一 (One To One)
- 一对多 (One To Many)
- 多对多 (Many To Many)



举个例子, 比如“用户-订单”这个一对多的关系中, 我们想查询某一个用户的所有或者某个订单, 我们可以

```javascript
var user_id = db.users.findOne( {username: "username_here"} )._id
db.orders.find( {user_id: user_id} )
```



## 4. MongoDB 的索引



<span style="color:#0000FF;font-size:16.0pt;font-weight:bold">4.1 概述</span>



索引支持在 MongoDB 中高效地执行查询.如果没有索引, MongoDB 必须执行全集合扫描, 即扫描集合中的每个文档, 以选择与查询语句 匹配的文档.这种扫描全集合的查询效率是非常低的, 特别在处理大量的数据时, 查询可以要花费几十秒甚至几分钟, 这对网站的性能是非常致命的. 



如果查询存在适当的索引, MongoDB 可以使用该索引限制必须检查的文档数. 



索引是特殊的数据结构, 它以易于遍历的形式存储集合数据集的一小部分.索引存储特定字段或一组字段的值, 按字段值排序.索引项的排 序支持有效的相等匹配和基于范围的查询操作.此外, MongoDB 还可以使用索引中的排序返回排序结果.



MongoDB 使用的是 B Tree, MySQL 使用的是 B+ Tree



```javascript
// create index
db.<collection_name>.createIndex({ userid : 1, username : -1 })

// retrieve indexes
db.<collection_name>.getIndexes()

// remove indexes
db.<collection_name>.dropIndex(index)

// there are 2 ways to remove indexes:
// 1. removed based on the index name
// 2. removed based on the fields

db.<collection_name>.dropIndex( "userid_1_username_-1" )
db.<collection_name>.dropIndex({ userid : 1, username : -1 })

// remove all the indexes, will only remove non_id indexes
db.<collection_name>.dropIndexes()
```



<span style="color:#0000FF;font-size:16.0pt;font-weight:bold">4.2 索引的类型</span>



<span style="color:#000000;font-size:14.0pt;font-weight:bold">4.2.1 单字段索引</span>



MongoDB 支持在文档的单个字段上创建用户定义的**升序/降序索引**, 称为**单字段索引** Single Field Index

对于单个字段索引和排序操作, 索引键的排序顺序（即升序或降序）并不重要, 因为 MongoDB 可以在任何方向上遍历索引.


![](F:\strato-sail\strato-sail.GitHub.io\img\in-post\mongodb\mongodb-05.png)

<span style="color:#000000;font-size:14.0pt;font-weight:bold">4.2.2 复合索引</span>

MongoDB 还支持多个字段的用户定义索引, 即复合索引 Compound Index

复合索引中列出的字段顺序具有重要意义.例如, 如果复合索引由 `{ userid: 1, score: -1 }` 组成, 则索引首先按 `userid` 正序排序, 然后 在每个 `userid` 的值内, 再在按 `score` 倒序排序.

![](F:\strato-sail\strato-sail.GitHub.io\img\in-post\mongodb\mongodb-06.png)

<span style="color:#000000;font-size:14.0pt;font-weight:bold">4.2.3 其他索引</span>

- 地理空间索引 Geospatial Index
- 文本索引 Text Indexes
- 哈希索引 Hashed Indexes

**地理空间索引（Geospatial Index）**

为了支持对地理空间坐标数据的有效查询, MongoDB 提供了两种特殊的索引: 返回结果时使用平面几何的二维索引和返回结果时使用球面几何的二维球面索引. 

**文本索引（Text Indexes）**

MongoDB 提供了一种文本索引类型, 支持在集合中搜索字符串内容.这些文本索引不存储特定于语言的停止词（例如 "the", "a", "or"）,  而将集合中的词作为词干, 只存储根词. 

**哈希索引（Hashed Indexes）**

为了支持基于散列的分片, MongoDB 提供了散列索引类型, 它对字段值的散列进行索引.这些索引在其范围内的值分布更加随机, 但只支持相等匹配, 不支持基于范围的查询.



<span style="color:#0000FF;font-size:16.0pt;font-weight:bold">4.3 索引的管理操作</span>



<span style="color:#000000;font-size:14.0pt;font-weight:bold">4.3.1 索引的查看</span>



语法

```
db.collection.getIndexes()
```



默认 `_id` 索引： MongoDB 在创建集合的过程中, 在 `_id` 字段上创建一个唯一的索引, 默认名字为 `_id` , 该索引可防止客户端插入两个具有相同值的文 档, 不能在 `_id` 字段上删除此索引. 



注意：该索引是**唯一索引**, 因此值不能重复, 即 `_id` 值不能重复的.

在分片集群中, 通常使用 `_id` 作为**片键**.



<span style="color:#000000;font-size:14.0pt;font-weight:bold">4.3.2 索引的创建</span>



语法

```
db.collection.createIndex(keys, options)
```

参数



![](F:\strato-sail\strato-sail.GitHub.io\img\in-post\mongodb\mongodb-07.png)



options（更多选项）列表



![](F:\strato-sail\strato-sail.GitHub.io\img\in-post\mongodb\mongodb-08.png)




注意在 3.0.0 版本前创建索引方法为 `db.collection.ensureIndex()` , 之后的版本使用了 `db.collection.createIndex()` 方法,  `ensureIndex()` 还能用, 但只是 `createIndex()` 的别名.



举个🌰

```sh
$  db.comment.createIndex({userid:1})
{
  "createdCollectionAutomatically" : false,
  "numIndexesBefore" : 1,
  "numIndexesAfter" : 2,
  "ok" : 1
}

$ db.comment.createIndex({userid:1,nickname:-1})
...

```



<span style="color:#000000;font-size:14.0pt;font-weight:bold">4.3.3 索引的删除</span>



语法

```sh
# 删除某一个索引
$ db.collection.dropIndex(index)

# 删除全部索引
$ db.collection.dropIndexes()
```



提示:

`_id` 的字段的索引是无法删除的, 只能删除非 `_id` 字段的索引



示例

```sh
# 删除 comment 集合中 userid 字段上的升序索引
$ db.comment.dropIndex({userid:1})
```



<span style="color:#0000FF;font-size:16.0pt;font-weight:bold">4.4 索引使用</span>



<span style="color:#000000;font-size:14.0pt;font-weight:bold">4.4.1 执行计划</span>



分析查询性能 (Analyze Query Performance) 通常使用执行计划 (解释计划 - Explain Plan) 来查看查询的情况

```shell
$ db.<collection_name>.find( query, options ).explain(options)
```


比如: 查看根据 `user_id` 查询数据的情况



**未添加索引之前**

`"stage" : "COLLSCAN"`, 表示全集合扫描


![](F:\strato-sail\strato-sail.GitHub.io\img\in-post\mongodb\mongodb-09.png)


**添加索引之后**

`"stage" : "IXSCAN"`, 基于索引的扫描



<span style="color:#000000;font-size:14.0pt;font-weight:bold">4.4.2 涵盖的查询</span>


当查询条件和查询的投影仅包含索引字段是, MongoDB 直接从索引返回结果, 而不扫描任何文档或将文档带入内存, 这些覆盖的查询十分有效

> https://docs.mongodb.com/manual/core/query-optimization/#covered-query





## 5. 案例



<span style="color:#0000FF;font-size:16.0pt;font-weight:bold">5.1 需求分析</span>

文章示例参考：早晨空腹喝水，是对还是错？https://www.toutiao.com/a6721476546088927748/

需要实现以下功能：

- 基本增删改查API
- 根据文章id查询评论
- 评论点赞



<span style="color:#0000FF;font-size:16.0pt;font-weight:bold">5.2 表结构分析</span>

![](F:\strato-sail\strato-sail.GitHub.io\img\in-post\mongodb\mongodb-10.png)



<span style="color:#0000FF;font-size:16.0pt;font-weight:bold">5.3 技术选型</span>

**SpringDataMongoDB**

SpringData家族成员之一，用于操作MongoDB的持久层框架，封装了底层的mongodb-driver。

官网主页： https://projects.spring.io/spring-data-mongodb/

我们十次方项目的吐槽微服务就采用SpringDataMongoDB框架。



<span style="color:#0000FF;font-size:16.0pt;font-weight:bold">5.4 文章微服务模块搭建</span>

<span style="color:#000000;font-size:14.0pt;font-weight:bold">5.4.1 创建项目 article</span>

- pom依赖

  ~~~xml
  <?xml version="1.0" encoding="UTF-8"?>
  
  <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
      <modelVersion>4.0.0</modelVersion>
  
      <groupId>com.wcy.article</groupId>
      <artifactId>article</artifactId>
      <version>1.0-SNAPSHOT</version>
  
      <parent>
          <groupId>org.springframework.boot</groupId>
          <artifactId>spring-boot-starter-parent</artifactId>
          <version>2.3.4.RELEASE</version>
          <relativePath/>
      </parent>
  
      <properties>
          <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
      </properties>
  
      <dependencies>
          <dependency>
              <groupId>org.springframework.boot</groupId>
              <artifactId>spring-boot-starter-test</artifactId>
          </dependency>
          <dependency>
              <groupId>org.springframework.boot</groupId>
              <artifactId>spring-boot-starter-data-mongodb</artifactId>
          </dependency>
      </dependencies>
  
  </project>
  ~~~

- 配置文件

  ~~~yaml
  spring:
    # 数据源配置
    data:
      mongodb:
        # 主机地址
        host: localhost
        # 数据库
        database: test
        # 默认端口号
        port: 27017
        # 也可以使用uri连接
        # uri: mongodb://192.168.1.222:27017/test
  ~~~

- 主启动类

  ~~~java
  @SpringBootApplication
  public class ArticleMainApplication {
      public static void main(String[] args) {
          SpringApplication.run(ArticleMainApplication.class, args);
      }
  }
  ~~~

- 启动项目无报错



<span style="color:#000000;font-size:14.0pt;font-weight:bold">5.4.2 实体类</span>

~~~java
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
//把一个java类声明为mongodb的文档，可以通过collection参数指定这个类对应的文档。
// @Document(collection="mongodb 对应 collection 名")
// 若未加 @Document ，该 bean save 到 mongo 的 comment collection
// 若添加 @Document ，则 save 到 comment collection 
@Document(collection="comment")
// 可以省略，如果省略，则默认使用类名小写映射集合
// 复合索引
// @CompoundIndex( def = "{'userid': 1, 'nickname': -1}")
public class Comment implements Serializable {
    //主键标识，该属性的值会自动对应mongodb的主键字段"_id"，如果该属性名就叫“id”,则该注解可以省略，否则必须写
    @Id
    private String id;//主键
    // 该属性对应mongodb的字段的名字，如果一致，则无需该注解
    @Field("content")
    private String content;//吐槽内容
    private Date publishtime;//发布日期
    // 添加了一个单字段的索引
    @Indexed
    private String userid;//发布人ID
    private String nickname;//昵称
    private LocalDateTime createdatetime;//评论的日期时间
    private Integer likenum;//点赞数
    private Integer replynum;//回复数
    private String state;//状态
    private String parentid;//上级ID
    private String articleid;
}
~~~

说明：

索引可以大大提升查询效率，一般在查询字段上添加索引，索引的添加可以通过Mongo的命令来添加，也可以在Java的实体类中通过注解添加。

1）单字段索引注解`@Indexed`

`org.springframework.data.mongodb.core.index.Indexed.class`

声明该字段需要索引，建索引可以大大的提高查询效率。

Mongo命令参考：

~~~shell
db.comment.createIndex({"userid":1})
~~~



2）复合索引注解`@CompoundIndex`

`org.springframework.data.mongodb.core.index.CompoundIndex.class`

复合索引的声明，建复合索引可以有效地提高多字段的查询效率。

Mongo命令参考：

~~~shell
db.comment.createIndex({"userid":1,"nickname":-1})
~~~



<span style="color:#000000;font-size:14.0pt;font-weight:bold">5.4.3 业务类</span>

- 创建dao包，并创建接口

  ~~~java
  import com.wcy.article.entities.Comment;
  import org.springframework.data.mongodb.repository.MongoRepository;
  import org.springframework.data.repository.PagingAndSortingRepository;
  
  public interface CommentRepository extends MongoRepository<Comment,String> {
  }
  ~~~

- 创建业务逻辑类service

  ~~~java
  import com.wcy.article.entities.Comment;
  import com.wcy.article.entities.dao.CommentRepository;
  import org.springframework.beans.factory.annotation.Autowired;
  import org.springframework.stereotype.Service;
  
  import java.util.List;
  
  @Service
  public class CommentService {
      @Autowired
      private CommentRepository commentRepository;
  
      /*** 保存一个评论 * @param comment */
      public void saveComment(Comment comment) {
          //如果需要自定义主键，可以在这里指定主键；如果不指定主键，MongoDB会自动生成主键
          // 设置一些默认初始值。。。
          // 调用dao
          commentRepository.save(comment);
      }
  
      /*** 更新评论 * @param comment */
      public void updateComment(Comment comment) {
          //调用dao
          commentRepository.save(comment);
      }
  
      /*** 根据id删除评论 * @param id */
      public void deleteCommentById(String id) {
          //调用dao
          commentRepository.deleteById(id);
      }
  
      /*** 查询所有评论 * @return */
      public List<Comment> findCommentList() {
          //调用dao
          return commentRepository.findAll();
      }
  
      /*** 根据id查询评论 * @param id * @return */
      public Comment findCommentById(String id) {
          //调用dao
          return commentRepository.findById(id).get();
      }
  }
  ~~~

- 测试类

  ~~~java
  package com.wcy.article.service;
  
  import com.wcy.article.entities.Comment;
  import org.junit.Test;
  import org.junit.runner.RunWith;
  import org.springframework.beans.factory.annotation.Autowired;
  import org.springframework.boot.test.context.SpringBootTest;
  import org.springframework.test.context.junit4.SpringRunner;
  
  import java.util.List;
  
  /**
   * @author wangcy
   * @date 2021-5-26 21:30
   * descriotion
   */
  @RunWith(SpringRunner.class)
  @SpringBootTest
  public class CommentServiceTest {
      @Autowired
      private CommentService commentService;
  
      @Test
      public void testFindCommentList(){
          List<Comment> commentList = commentService.findCommentList();
          for (Comment c:commentList){
              System.out.println(c);
          }
      }
  }
  ~~~

  

<span style="color:#000000;font-size:14.0pt;font-weight:bold">5.4.4 根据上级ID查询文章评论的分页列表</span>

- CommentRepository 新增方法定义：`findByParentid `方法名有规范

  ~~~java
  public interface CommentRepository extends MongoRepository<Comment,String> {
      //根据父id，查询子评论的分页列表 
      Page<Comment> findByParentid(String parentid, Pageable pageable);
  }
  ~~~

- CommentService 新增方法：

  ~~~java
  /** 根据父id查询分页列表 
   * @param parentid 
   * @param page 
   * @param size 
   * @return 
   */
  public Page<Comment> findCommentListPageByParentid(String parentid, int page, int size) {
      return commentRepository.findByParentid(parentid, PageRequest.of(page - 1, size));
  }
  ~~~

- 测试：

  ~~~java
  @Test
  public void testFindCommentListPageByParentid() {
      Page<Comment> pageResponse = commentService.findCommentListPageByParentid("3", 1, 2);
      System.out.println("----总记录数：" + pageResponse.getTotalElements());
      System.out.println("----当前页数据：" + pageResponse.getContent());
  }
  ~~~



<span style="color:#000000;font-size:14.0pt;font-weight:bold">5.4.5 MongoTemplate 实现评论点赞</span>

~~~java
/*** 点赞-效率低 * @param id */ 
public void updateCommentThumbupToIncrementingOld(String id){ 
    Comment comment = CommentRepository.findById(id).get();
    comment.setLikenum(comment.getLikenum()+1); 
    CommentRepository.save(comment); 
}
~~~

以上方法虽然简单，但执行效率较低，使用下面MongoTemplate 的 inc 方法（CommentService.java添加）：

~~~java
/*** 点赞数+1 * @param id */
    public void updateCommentLikenum(String id) {
        //查询对象
        Query query = Query.query(Criteria.where("_id").is(id));
        //更新对象
        Update update = new Update();
        //局部更新，相当于$set
        // update.set(key,value)
        // 递增$inc
        // update.inc("likenum",1);
        update.inc("likenum");
        //参数1：查询对象
        // 参数2：更新对象
        // 参数3：集合的名字或实体类的类型Comment.class
        mongoTemplate.updateFirst(query, update, "comment");
    }
~~~





# MongoDB集群和安全



- MongoDB的副本集：操作、主要概念、故障转移、选举规则
- MongoDB的分片集群：概念、优点、操作、分片策略、故障转移
- MongoDB的安全认证



# * 遇到的坑

- mongodb 配置文件启动时，不能识别`tab`键，用空格代替。

