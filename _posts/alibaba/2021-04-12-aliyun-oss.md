---
layout:     post
title:      "aliyun oss简介与使用"
subtitle:   "对阿里云对象存储oss的介绍以及使用过程"
date:       2021-04-12 12:00:00
author:     "Wangcy"
catalog: false
header-style: text
tags:
  - 对象存储 
  - oss
  - 阿里
---

阿里云官方文档：https://help.aliyun.com/product/31815.html?spm=a2c4g.11186623.6.540.45374717z7kQqI

# 介绍



## 1.对象存储oss提供的场景

| 类型         | 介绍                                                         |
| -------------------- | ------------------------------------------------------------ |
| 标准存储类型 | 高持久、高可用、高性能的对象存储服务，支持频繁的数据访问。是各种社交、分享类的图片、音视频应用、大型网站、大数据分析的合适选择。 |
| 低频访问存储类型 | 适合长期保存不经常访问的数据（平均每月访问频率1到2次）。存储单价低于标准类型，适合各类移动应用、智能设备、企业数据的长期备份，支持实时数据访问。 |
| 归档存储类型 | 适合需要长期保存（建议半年以上）的归档数据，在存储周期内极少被访问，数据进入到可读取状态需要1分钟的解冻时间。适合需要长期保存的档案数据、医疗影像、科学资料、影视素材。 |
| 冷归档存储类型 | 适合需要超长时间存放的极冷数据。例如因合规要求需要长期留存的数据、大数据及人工智能领域长期积累的原始数据、影视行业长期留存的媒体资源、在线教育行业的归档视频等。 |

## 2.场景

OSS适用于以下场景：

- 静态网站内容和音视频的存储与分发

  每个存储在OSS上的文件（Object）都有唯一的HTTP URL地址，用于内容分发。同时，OSS还可以作为内容分发网络（CDN）的源站。由于无需分区，OSS尤其适用于托管那些数据密集型、用户生产内容的网站，如图片和视频分享网站。各种终端设备、Web网站程序、移动应用可以直接向OSS写入或读取数据。OSS支持流式写入和文件写入两种方式。

- 静态网站托管

  作为低成本、高可用、高扩展性的解决方案，OSS可用于存储静态HTML文件、图片、视频、JavaScript等类型的客户端脚本。

- 计算和分析的数据存储仓库

  OSS的水平扩展性使您可以同时从多个计算节点访问数据而不受单个节点的限制。

- 数据备份和归档

  OSS为重要数据的备份和归档提供高可用、可扩展、安全可靠的解决方案。您可以通过设置生命周期规则将存储在OSS上的冷数据自动转储为低频或者归档存储类型以节约存储成本。您还可以使用跨区域复制功能在不同地域的不同存储空间之间自动异步（近实时）复制数据，实现业务的跨区域容灾。

## 3.基本概念

- 存储空间（Bucket）：存储对象的容器，存储空间拥有一些配置（访问权限，存储类型）。

- 对象（Object）：由元信息Meta，用户数据Data和文件名Key组成。

- ObjectKey：相当于静态资源路径 abc/efg/123.jpg。

- 地域（Region）：数据中心所在物理位置

- 访问域名（Endpoint）：表示对外服务的访问域名，RestFul风格提供服务。内网外网的访问域名不同。

  ```XML
  <Schema>://<Bucket>.<外网Endpoint>/<Object> 
  <Schema>://<Bucket>.<内网Endpoint>/<Object>
  <!-- 如果您的Region为华东1（杭州），Bucket名称为examplebucket，Object访问路径为example/example.txt -->
  https://examplebucket.oss-cn-hangzhou.aliyuncs.com/example/example.txt
  ```

  ```java
  String accessKeyId = "<key>";
  String accessKeySecret = "<secret>";
  String endpoint = "oss-cn-hangzhou-internal.aliyuncs.com";
  OSSClient client = new OSSClient(endpoint, accessKeyId, accessKeySecret);
  ```

- 访问密钥（AccessKey）：每次建立客户端连接需要AccessKeyId和AccessKeySecret，用来验证身份。

# 使用

[开放式存储 Java API手册 1.0](http://aliyun_portal_storage.oss.aliyuncs.com/oss_api/oss_javahtml/object.html)，[阿里云官网API](https://help.aliyun.com/document_detail/32008.html)

## 1.构建项目

使用springboot+maven构建

### 前期准备

从阿里云平台获取到`endpoint`、`accessKeyId`、`accekkKeySecret`相关配置。

创建bucket(存储命名空间、平台唯一才行)，可以在阿里云操作台建立，也可以通过代码生成。

### Maven依赖

~~~xml
<!-- https://mvnrepository.com/artifact/com.aliyun.oss/aliyun-sdk-oss -->
<dependency>
    <groupId>com.aliyun.oss</groupId>
    <artifactId>aliyun-sdk-oss</artifactId>
    <version>3.8.0</version>
</dependency>
~~~

### 属性配置

将和环境相关的固定配置类信息放置于properties文件中：

~~~yml
# aliyun oss
aliyun:
  endpoint: http://oss-cn-hangzhou.aliyuncs.com
  accessKeyId: LTAI5t8p6pZv9FmLBgMeTqxG
  accessKeySecret: sPDeYGOrYlrJZ32vjZvKWY25IO982w
  bucketName: wcy-bucket
  fileHost: dev
~~~

## 2.配置类

使用`Lombok`生成`getter`和`setter`，`@ConfigurationProperties`使用`getter`和`setter`注入

~~~java
@ToString
@Data
@Component
@ConfigurationProperties(prefix = "aliyun")
public class OSSConfig {
    String endpoint;
    String accessKeyId;
    String accessKeySecret;
    String bucketName;
    String fileHost;
}
~~~

## 3.工具类

~~~Java

~~~



## 4.上传文件

~~~java
/**
     * 核心上传功能
     * @param file     File类型本地文件
     * @param fileType 文件类型
     * @param fileName 云端文件名
     * @return
     */
    public static String putObject(File file, String fileType, String fileName) {
        String url = null;
        OSS ossClient = null;
        try {
            //ossClient = new OSSClient(ossConfig.getEndpoint(), ossConfig.getAccessKeyId(), ossConfig.getAccessKeySecret());//过时了
            ossClient = new OSSClientBuilder().build(ossConfig.getEndpoint(), ossConfig.getAccessKeyId(), ossConfig.getAccessKeySecret());
            InputStream input = new FileInputStream(file);
            ObjectMetadata meta = new ObjectMetadata();                // 创建上传Object的Metadata
            meta.setContentType(AliyunOssUtils.contentType(fileType));        // 设置上传内容类型
            meta.setCacheControl("no-cache");                    // 被下载时网页的缓存行为
            PutObjectRequest request = new PutObjectRequest(ossConfig.getBucketName(), fileName, input, meta);            //创建上传请求
            ossClient.putObject(request);
            url = ossConfig.getEndpoint().replaceFirst("http://", "http://" + ossConfig.getBucketName() + ".") + "/" + fileName;        //上传成功再返回的文件路径
        } catch (OSSException | FileNotFoundException oe) {
            oe.printStackTrace();
            return null;
        } finally {
            ossClient.shutdown();
        }
        return url;
    }


    private static String contentType(String fileType) {
        fileType = fileType.toLowerCase();
        String contentType = "";
        switch (fileType) {
            case "bmp":
                contentType = "image/bmp";
                break;
            case "gif":
                contentType = "image/gif";
                break;
            case "png":
            case "jpeg":
            case "jpg":
                contentType = "image/jpeg";
                break;
            case "html":
                contentType = "text/html";
                break;
            case "txt":
                contentType = "text/plain";
                break;
            case "vsd":
                contentType = "application/vnd.visio";
                break;
            case "ppt":
            case "pptx":
                contentType = "application/vnd.ms-powerpoint";
                break;
            case "doc":
            case "docx":
                contentType = "application/msword";
                break;
            case "xml":
                contentType = "text/xml";
                break;
            case "mp4":
                contentType = "video/mp4";
                break;
            default:
                contentType = "application/octet-stream";
                break;
        }
        return contentType;
    }
~~~

~~~java
//文件上传
File file = new File("filepath");
System.out.println(AliyunOssUtils.putObject(file, "jpg", "fo.jpg"));
~~~

## 5.列出某个bucket所有对象

~~~java
/**
* 列出buckets下的所有文件
* @param ossClient 连接
* @param bucketName bucketName
*/
    public void listObjects(OSS ossClient, String bucketName) {
        System.out.println("Listing objects");
        ObjectListing objectListing = ossClient.listObjects(bucketName);
        for (OSSObjectSummary objectSummary : objectListing.getObjectSummaries()) {
            System.out.println(" - " + objectSummary.getKey() + "  " +
                    "(size = " + objectSummary.getSize() + ")");
        }
        System.out.println();
    }
~~~

~~~Java
public static OSSConfig ossConfig;
public static AliyunOssUtils aliyunOssUtils;
//注入
public SpringbootdemoApplication(AliyunOssUtils aliyunOssUtils, OSSConfig ossConfig) {
    this.aliyunOssUtils = aliyunOssUtils;
    this.ossConfig = ossConfig;
}

//列出bucket所有对象
OSS ossClient = new OSSClientBuilder().build(ossConfig.getEndpoint(), ossConfig.getAccessKeyId(), ossConfig.getAccessKeySecret());
aliyunOssUtils.listObjects(ossClient, ossConfig.getBucketName());
~~~

~~~java
//控制台输出
Listing objects
 - favicon.png  (size = 109527)
 - fo.jpg  (size = 19113)
~~~

## 6.列出当前用户所有bucket

~~~java
	/**
     * 列出当前用户下的所有bucket
     * @param ossClient
     */
    public void listBuckets(OSS ossClient) {
        System.out.println("Listing buckets");
        ListBucketsRequest listBucketsRequest = new ListBucketsRequest();
        listBucketsRequest.setMaxKeys(500);
        for (Bucket bucket : ossClient.listBuckets()) {
            System.out.println(" - " + bucket.getName());
        }
        System.out.println();
    }
~~~

## 7.根据资源url删除文件

~~~java
	/**
     * @param fileUrl 需要删除的文件url
     * @return boolean 是否删除成功
     * @MethodName: deleteFile
     * @Description: 单文件删除
     */
    public boolean deleteFile(String fileUrl) {
        String bucketName = getBucketName(fileUrl);        //根据url获取bucketName
        String fileName = getFileName(fileUrl);            //根据url获取fileName
        if (bucketName == null || fileName == null) return false;
        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(ossConfig.getEndpoint(), ossConfig.getAccessKeyId(), ossConfig.getAccessKeySecret());
            GenericRequest request = new DeleteObjectsRequest(bucketName).withKey(fileName);
            ossClient.deleteObject(request);
        } catch (Exception oe) {
            oe.printStackTrace();
            return false;
        } finally {
            ossClient.shutdown();
        }
        return true;
    }

	/**
     * @param fileUrl 文件url
     * @return String bucketName
     * @MethodName: getBucketName
     * @Description: 根据url获取bucketName
     */
    public String getBucketName(String fileUrl) {
        String http = "http://";
        String https = "https://";
        int httpIndex = fileUrl.indexOf(http);
        int httpsIndex = fileUrl.indexOf(https);
        int startIndex = 0;
        if (httpIndex == -1) {
            if (httpsIndex == -1) {
                return null;
            } else {
                startIndex = httpsIndex + https.length();
            }
        } else {
            startIndex = httpIndex + http.length();
        }
        int endIndex = fileUrl.indexOf(".oss-");
        return fileUrl.substring(startIndex, endIndex);
    }

    /**
     * @param fileUrl 文件url
     * @return String fileName
     * @MethodName: getFileName
     * @Description: 根据url获取fileName
     */
    private String getFileName(String fileUrl) {
        String str = "aliyuncs.com/";
        int beginIndex = fileUrl.indexOf(str);
        if (beginIndex == -1) return null;
        return fileUrl.substring(beginIndex + str.length());
    }		
~~~



