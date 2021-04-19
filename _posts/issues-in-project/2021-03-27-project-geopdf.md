---
layout:     post
title:      "geoPdf 地图切片程序"
subtitle:   "geoPdf project"
date:       2021-03-27 12:00:00
author:     "Wangcy"
catalog: false
header-style: text
tags:
  - c++ 
  - tinyxml
  - PDFium
  - 共享内存
  - geopdf
---



## 综述

此项目是在刚上研二时接到手的，一句话总结就是**将测绘所发布的`GeoPDF`格式地图转换成[瓦片地图](https://www.jianshu.com/p/e9e83b427045)格式，同时拥有开始、暂停、继续等外部命令**。

**本文涉及到项目中的功能包括：**

1. 用`tinyxml`库解析操作`xml`
2. `Windows`下共享内存的操作
3. 使用福昕库`PDFium`提取`PDF`对象信息
4. 使用`png`库对图片操作

## 使用tinyxml库

项目中`xml`示例

```xml
<!-- 要读取的Xml文件 temp-->
<?xml version="1.0" encoding="UTF-8" standalone="yes" ?>
<Map>
    <Requests>
        <!-- 源pdf的绝对路径 -->
        <pdfPath>D:\temp\test.pdf</pdfPath>
        <!-- 保存加底图后的png绝对路径 -->
        <pngPath>D:\temp\</pngPath>
        <!-- 生成的瓦片文件夹绝对路径 -->
        <tilePath>D:\temp\tilee\</tilePath>
        <!-- 瓦片大小，宽高 -->
        <tileSize tileHeight="256" tileWidth="256" />
        <!-- 生成瓦片格式  PNG是4通道RGBA -->
        <tileType>PNG</tileType>
        <Range>
            <Longitude>
                <min>111.66722</min>
                <max>111.86667</max>
            </Longitude>
            <Latitude>
                <min>21.497223</min>
                <max>21.762223</max>
            </Latitude>
        </Range>
        <!-- 层级，目前支持最大15 -->
        <Level>
            <min>0</min>
            <max>14</max>
        </Level>
    </Requests>
    <!-- 正在进行的任务，TaskName是共享内存的lpName -->
    <Tasks>
        <aaa />
        <bbb />
    </Tasks>
</Map>
```



解析`xml`里数据，并保存在对象属性。

以下代码作用：将经纬度信息保存

```c++
TiXmlDocument doc(XMLPATH);    // 读入XML文件
//TiXmlDocument doc("F:\\config.xml");    // 读入XML文件
if (!doc.LoadFile()) throw exception("无法读取配置文件，退出");  // 如果无法读取文件，则返回
TiXmlHandle hDoc(&doc);         // hDoc是&doc指向的对象
TiXmlElement* pElem;            // 指向元素的指针
pElem = hDoc.FirstChildElement().Element(); //指向根节点
TiXmlHandle hRoot(pElem);       // hRoot是根节点

TiXmlElement* rangeElem = hRoot.FirstChild("Requests").FirstChild("Range").Element();
TiXmlHandle range(rangeElem); //Range结点
TiXmlElement* minLongitudeElem = range.FirstChild("Longitude").FirstChild("min").Element();
TiXmlElement* maxLongitudeElem = range.FirstChild("Longitude").FirstChild("max").Element();
TiXmlElement* minLatitudeElem = range.FirstChild("Latitude").FirstChild("min").Element();
TiXmlElement* maxLatitudeElem = range.FirstChild("Latitude").FirstChild("max").Element();
//string temp = minLongitudeElem->GetText();
//minLongitude = atof(temp.c_str());
minLongitude = atof(minLongitudeElem->GetText());   //string转为double
maxLongitude = atof(maxLongitudeElem->GetText());
minLatitude = atof(minLatitudeElem->GetText());
maxLatitude = atof(maxLatitudeElem->GetText());
```

因为项目需要列出所有正在运行的切片任务，所以在`xml`里添加`Tasks`结点，里面每一个结点都是一个任务。

以下代码为`xml`中对结点的增删改查。

当一个切图任务开始，则在`Tasks`结点下增加一个新结点，任务名为task＋时间戳`。

```c++
string uuid::createUIDtime()  //任务名为task＋时间戳
{
	time_t mytime = time(NULL);
	LONGLONG tt = time(&mytime);
	string ret = "task" + to_string(tt);
	return ret;
}
```

其中考虑到**暂停、继续**功能于是功能实现思路，每次暂停后在任务结点的`text`里填写已经切到多少级，下次继续时直接读取此层级继续向更高层级切瓦片。

```c++
//添加任务结点，name是task的uuid也是共享内存的lpName
void ParseXML::addTaskNode(string name)
{
	TiXmlDocument doc(XMLPATH);    // 读入XML文件
	if (!doc.LoadFile()) throw exception("无法读取配置文件，退出");  // 如果无法读取文件，则返回
	TiXmlElement *MapRoot = doc.FirstChildElement(); //指向根节点
	TiXmlElement *TasksNode = MapRoot->FirstChildElement("Tasks");
	TiXmlElement *tempNode = new TiXmlElement(name.c_str());  //创建新节点 
	tempNode->LinkEndChild(new TiXmlText(""));
	TasksNode->LinkEndChild(tempNode);
	doc.SaveFile(XMLPATH);
}

//删除任务结点（当任务结束），name是task的uuid也是共享内存的lpName
void ParseXML::deleteTaskNode(string name) {
	TiXmlDocument doc(XMLPATH);    // 读入XML文件
	if (!doc.LoadFile()) throw exception("无法读取配置文件，退出");  // 如果无法读取文件，则返回
	TiXmlElement *MapRoot = doc.FirstChildElement(); //指向根节点
	TiXmlElement *TasksNode = MapRoot->FirstChildElement("Tasks");
	TiXmlElement *remove = TasksNode->FirstChildElement(name.c_str());
	TasksNode->RemoveChild(remove);
	doc.SaveFile(XMLPATH);
}

//列出所有正在进行的任务
vector<string> ParseXML::listTaskNide()
{
	vector<string> ret;
	TiXmlDocument doc(XMLPATH);    // 读入XML文件
	if (!doc.LoadFile()) throw exception("无法读取配置文件，退出");  // 如果无法读取文件，则返回
	TiXmlElement *MapRoot = doc.FirstChildElement(); //指向根节点
	TiXmlElement *TasksNode = MapRoot->FirstChildElement("Tasks");
	//TiXmlElement *tempEle = NULL;
	for (TiXmlElement *nodeEle = TasksNode->FirstChildElement(); nodeEle; nodeEle = nodeEle->NextSiblingElement())
	{
		/*tempEle = nodeEle;
		if (nodeEle->GetText() != NULL)
		{
			string tag = nodeEle->Value();
			string content = nodeEle->GetText();
		}*/
		string tag = nodeEle->Value();
		ret.push_back(tag);
	}
	return ret;
}

//暂停命令，暂存切到多少级<node>12<node/>代表切到12级了
void ParseXML::setStoppedLevel(string name, int level) {

	string strName(name);

	TiXmlDocument doc(XMLPATH);    // 读入XML文件
	if (!doc.LoadFile()) throw exception("无法读取配置文件，退出");  // 如果无法读取文件，则返回
	TiXmlElement *MapRoot = doc.FirstChildElement(); //指向根节点
	TiXmlElement *TasksNode = MapRoot->FirstChildElement("Tasks");

	//int to const char *
	char a[8];
	const char *levelTemp = itoa(level, a, 10);

	TiXmlElement *remove = TasksNode->FirstChildElement(strName.c_str());
	TasksNode->RemoveChild(remove);
	TiXmlElement *tempNode = new TiXmlElement(strName.c_str());  //创建新节点 
	tempNode->LinkEndChild(new TiXmlText(levelTemp));
	TasksNode->LinkEndChild(tempNode);
	doc.SaveFile(XMLPATH);
}

//继续命令，得到此任务已经切到多少级
int ParseXML::getStoppedLevel(string name) {
	string strName(name);

	TiXmlDocument doc(XMLPATH);    // 读入XML文件
	if (!doc.LoadFile()) throw exception("无法读取配置文件，退出");  // 如果无法读取文件，则返回
	TiXmlElement *MapRoot = doc.FirstChildElement(); //指向根节点
	TiXmlElement *TasksNode = MapRoot->FirstChildElement("Tasks");
	TiXmlElement *Task = TasksNode->FirstChildElement(strName.c_str());
	const char * str = Task->GetText();

	int ret = atoi(str);

	return ret;
}
```



## Windows共享内存

> **参考的链接：**
>
> https://blog.csdn.net/u013052326/article/details/76359588
>
> https://blog.csdn.net/zsc_976529378/article/details/52604973
>
> https://blog.csdn.net/xialianggang1314/article/details/78477451

因为要有任务的开启、暂停、继续、取消等功能。本来的思路是在`xml`里加一个状态信息，在切图程序运行中不断判断这个值，根据这个状态值而改变程序的运行状态。如下图所示：

![](..\..\img\in-post\project\post-geopdf-01.png)

之后发现此方案不妥：

- 不能进行并发切瓦片
- 造成大量io操作，资源浪费

于是采取**切图控制程序+切图程序**方案，两个进程同时工作，采用共享内存进行线程间通信，切图控制程序生命周期较短，切图程序生命周期较长，切图控制程序主要修改共享内存中的状态码。共享内存只有两个数据：`state, process`，状态码和切图进度。如下图：

![](..\..\img\in-post\project\post-geopdf-02.png)

共享内存操作主要的函数，头文件如下：

```c++
class ShareMemory
{
public:
	ShareMemory();
	void writeMemory(string name, shareData data);  //写标记符为name的共享内存
	shareData readMemory(string name);   //读标记符为name的共享内存
	void writeState(string name, char state);  //只写name共享内存的state
	void writePercentage(string name, char levelchar);  //只写name共享内存的percentage
	char readState(string name);  //只读name共享内存中的state
	char readPercentage(string name);  //只读name共享内存中的levelchar
	void unmapAndClose(string name);  //释放名为name的共享内存 (撤销文件视图，并且关闭映射文件句柄）
private:
	HANDLE m_hLock;  //创建锁的句柄
	std::wstring m_strName;   //锁的名字，有了相同的名字, 在跨进程加锁的时候, 就可以得到同一把锁
	BOOL LOCK(DWORD dwTime);    //共享内存上锁
	void UNLOCK();   //解锁
};
```

功能函数如下，开启新的共享内存、设置共享内存的值、读取共享内存的值：

```c++
void ShareMemory::writeMemory(string name, shareData data)
{
	string strName(name);

	string strData;//用于存储写入数据
	strData.append(1, data.state);
	strData.append(1, data.levelchar);
	LPVOID pBuffer;// 共享内存指针
	HANDLE hMap;//定义一个句柄

	LOCK(INFINITE);  //上锁

					 //getline(cin, strData);//读取一行数据给strData
	hMap = ::CreateFileMapping(INVALID_HANDLE_VALUE, NULL, PAGE_READWRITE, 0,
		strData.size(),
		(LPCWSTR)strName.c_str());
	pBuffer = ::MapViewOfFile(hMap, FILE_MAP_ALL_ACCESS, 0, 0, 0);//得到与共享内存映射的指针
	strcpy((char*)pBuffer, strData.c_str());//写入数据
	cout << "写入共享内存数据：" << (char *)pBuffer << endl;
	//system("pause");
	//::UnmapViewOfFile(pBuffer);//停止指针到共享内存的映射
	//::CloseHandle(hMap);//关闭共享内存的句柄

	UNLOCK();   //取消锁
}

shareData ShareMemory::readMemory(string name)
{
	shareData retData;//用于存储共享内存中的数据
	string strData;

	string strName(name);

	LPVOID pBuffer = NULL;// 共享内存指针 

	LOCK(INFINITE);

	HANDLE hMap = ::OpenFileMapping(FILE_MAP_ALL_ACCESS, 0, (LPCWSTR)strName.c_str());// 先判断要打开的共享内存名称是否存在
	if (NULL == hMap)
	{
		cout << "尚未创建共享内存" << endl;
	}
	else
	{    //共享内存存在，获得指向共享内存的指针，显示出数据
		pBuffer = ::MapViewOfFile(hMap, FILE_MAP_ALL_ACCESS, 0, 0, 0);
		strData = (char *)pBuffer;
		//cout << "读取出共享内存数据：" << strData << endl;
		retData.state = strData[0];
		retData.levelchar = strData[1];
		cout << "state:" << retData.state << endl;
		cout << "levelchar:" << retData.levelchar << endl;
	}
	//::UnmapViewOfFile(pBuffer);//停止指针到共享内存的映射
	//::CloseHandle(hMap);//关闭共享内存的句柄

	UNLOCK();

	return retData;
}

//只写name共享内存的state
void ShareMemory::writeState(string name, char state)
{
	string strName(name);
	shareData data = readMemory(strName);
	data.state = state;
	writeMemory(strName, data);
}

//只写name共享内存的percentage
void ShareMemory::writePercentage(string name, char levelchar)
{
	string strName(name);
	shareData data = readMemory(strName);
	data.levelchar = levelchar;
	writeMemory(strName, data);
}

//只读name共享内存中的state
char ShareMemory::readState(string name)
{
	string strName(name);
	shareData data = readMemory(strName);
	return data.state;
}

//只读name共享内存中的percentage
char ShareMemory::readPercentage(string name)
{
	string strName(name);
	shareData data = readMemory(strName);
	return data.levelchar;
}

//释放名为name的共享内存 (撤销文件视图，并且关闭映射文件句柄）
void ShareMemory::unmapAndClose(string name)
{
	HANDLE hMap = ::OpenFileMapping(FILE_MAP_ALL_ACCESS, 0, (LPCWSTR)name.c_str());// 先判断要打开的共享内存名称是否存在
	if (NULL == hMap)
	{
		cout << "尚未创建共享内存" << endl;
	}
	LPVOID pBuffer = ::MapViewOfFile(hMap, FILE_MAP_ALL_ACCESS, 0, 0, 0);//得到与共享内存映射的指针

																		 //::UnmapViewOfFile(pBuffer);//停止指针到共享内存的映射
																		 //::CloseHandle(hMap);//关闭共享内存的句柄

	if (pBuffer)
	{
		::UnmapViewOfFile(pBuffer);  //撤销文件视图
		pBuffer = NULL;
	}
	if (hMap)
	{
		::CloseHandle(hMap);  //关闭映射文件句柄
		hMap = NULL;
	}
}

int ShareMemory::reverseInteger(int x) {  //整数反转
	int fanz = 0;
	int c;
	while (x) {
		c = fanz;
		fanz = fanz * 10 + x % 10;
		if (fanz / 10 != c) {
			return 0;
		}
		x = x / 10;
	}
	return fanz;
}

//上锁
BOOL ShareMemory::LOCK(DWORD dwTime)
{
	// 如果还没有创建锁就先创建一个
	if (!m_hLock)
	{
		//lpName是指定这把锁的名字，你要不给这把锁取个名字都可以，只是有了相同的名字，在跨进程加锁的时候，就可以得到同一把锁。
		std::wstring strLockName = m_strName;
		strLockName.append(L"_Lock");
		// 初始化的时候不被任何线程占用
		m_hLock = ::CreateMutexW(NULL, FALSE, strLockName.c_str());
		if (!m_hLock)
			return FALSE;
	}

	// 哪个线程最先调用等待函数就最先占用这个互斥量
	DWORD dwRet = ::WaitForSingleObject(m_hLock, dwTime);
	return (dwRet == WAIT_OBJECT_0 || dwRet == WAIT_ABANDONED);
}

//解锁
void ShareMemory::UNLOCK()
{
	if (m_hLock)
	{
		::ReleaseMutex(m_hLock);
	}
}
```

