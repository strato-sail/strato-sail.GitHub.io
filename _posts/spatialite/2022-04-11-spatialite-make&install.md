---
layout:     post
title:      "Spatialite空间数据库编译和使用"
subtitle:   "Spatialite make&install"
date:       2022-04-11 12:00:00
author:     "Wangcy"
catalog: false
header-style: text
tags:
  - spatialite 
  - c++
  - 空间数据
---

国内资料太少，过程比较艰辛。



# 1.编译生成阶段 64位



https://www.jianshu.com/p/3bd34bb4bc02?tdsourcetag=s_pctim_aiomsg

https://blog.csdn.net/shixinwenwu/article/details/86290115

根据这个网站完成50%



使用OSGeo4W工具安装所有需要的依赖 **proj**、geos、freexl、iconv、sqlite3、zlib、libxml2



因为要适配vs2010，所以选择低版本的spatialite（4.3.0a）



其中spatialite4.3版本需要proj4，高版本的proj不支持会编译失败
而使用下载工具OSGeo4w只能下载proj9



删除proj9后下载proj源码，手动编译生成proj4，使用vc的编译器nmake编译，放到指定目录后修改spatialite的makefile.vc的proj4的路径



# 2.vs2010 64位调用



vs2010编译器v100



在项目文件夹创建`lib, include, bin`三个文件夹，并将第一步的所有依赖拷贝

由于是x64，于是将dll同样拷贝到`x64/Debug`文件夹里，与项目exe放在一起



解决方案 --> c/c++ --> 常规 --> 附加包含目录：

~~~
D:\visual studio 2015\Projects\sqliteDemo1\include
D:\visual studio 2015\Projects\sqliteDemo1\include\geos
D:\visual studio 2015\Projects\sqliteDemo1\include\libxml
D:\visual studio 2015\Projects\sqliteDemo1\include\libxml2
D:\visual studio 2015\Projects\sqliteDemo1\include\proj
D:\visual studio 2015\Projects\sqliteDemo1\include\spatialite
~~~



解决方案 --> 连接器 --> 常规 --> 附加包含目录：

~~~
D:\visual studio 2015\Projects\sqliteDemo1\lib
~~~



解决方案 --> 连接器 --> 输入 --> 附加依赖项：（这里使用`xxx_i.lib`，尝试放`xxx.lib`会报错）

~~~
sqlite3_i.lib
proj.lib
geos.lib
geos_c.lib
iconv.lib
freexl_i.lib
zlib.lib
libxml2.lib
spatialite.lib
~~~



解决方案 -->  c/c++ --> 代码生成 -->运行库：

~~~
选择：多线程调试DLL（/MDd）
因为在编译阶段就是使用/MD参数进行编译
~~~



在网上下载`shapefile`格式文件，并使用`spatialite_gui.exe`导入某个`shape`到新建数据库里

（注意`shapefile`文件路径不能有中文）

SIR选择4326，导入成功后数据库表最后一列会生成名为`geometry`的`BLOB`数据库类型



测试代码：

```c++
// sqliteDemo1.cpp : 定义控制台应用程序的入口点。
//

#include "stdafx.h"
#include "sqlite3.h"
#include "spatialite/gaiageo.h"
#include "spatialite/gg_const.h"
#include "spatialite.h"
#include <vector>
#include <iostream>

using namespace std;

typedef int int32_t;
typedef unsigned char uchar;

struct PointD
{
	double x;
	double y;
	double z;
};

//这个函数演示spatialite读取数据库中坐标的方法
void getGeometryPoints(std::vector<PointD> &points, sqlite3_stmt *stmt, int index)
{
	uchar *pBolb = (uchar *)sqlite3_column_blob(stmt, index);
	if (pBolb != nullptr)
	{
		int count = sqlite3_column_bytes(stmt, index);
		gaiaGeomCollPtr geo = nullptr;
		geo = gaiaFromSpatiaLiteBlobWkb(pBolb, count);
		if (geo != nullptr)
		{
			if (geo->DimensionModel == GAIA_XY)
			{
				int geoType = gaiaGeometryType(geo);
				if (geoType == GAIA_POINTZ) // 读取pointz
				{
					gaiaPointPtr p = geo->FirstPoint;
					if (p != nullptr)
					{
						if (p->DimensionModel == GAIA_XY)
						{
							PointD pt;
							pt.x = p->X;
							pt.y = p->Y;
							pt.z = p->Z;
							points.emplace_back(pt);
						}
						p = p->Next;
					}
				}
				else if (geoType == GAIA_LINESTRING) // 读取linestringz
				{
					gaiaLinestringPtr lp = geo->FirstLinestring;
					while (lp != nullptr)
					{
						for (int i = 0; i < lp->Points; i++)
						{
							if (lp->DimensionModel == GAIA_XY)
							{
								PointD pt;
								pt.x = (lp->Coords)[i * 3];
								pt.y = (lp->Coords)[i * 3 + 1];
								points.emplace_back(pt);
							}
						}
						lp = lp->Next;
					}
				}
				else if (geoType == GAIA_POLYGONZ) // 读取polygonz
				{
					gaiaPolygonPtr poly = geo->FirstPolygon;
					while (poly != nullptr)
					{
						gaiaRingPtr pRing = poly->Exterior;
						for (int i = 0; i < pRing->Points; i++)
						{
							PointD pt;
							pt.x = (pRing->Coords)[i * 3];
							pt.y = (pRing->Coords)[i * 3 + 1];
							pt.z = (pRing->Coords)[i * 3 + 2];
							points.emplace_back(pt);
						}
						poly = poly->Next;
					}
				}
				else
				{
					std::cout << "not finish geoType value is: " << geoType << std::endl;
				}
			}
			else
			{
				std::cout << "geo->DimensionModel != GAIA_XY_Z" << std::endl;
			}
		}
	}
}
//sqlite3结合spatialite使用的基本操作方法
void readDbData(const char* dbFile)
{
	sqlite3 *db = nullptr;
	spatialite_init(0);
	int32_t ret = sqlite3_open_v2(dbFile, &db, SQLITE_OPEN_READONLY, nullptr);
	if (ret != SQLITE_OK)
	{
		std::cout << "connect database " << dbFile << " failed: %s" << sqlite3_errstr(ret) << std::endl;
	}
	sqlite3_stmt *stmt = nullptr;
	int result = sqlite3_prepare_v2(db, "SELECT * FROM aaa", -1, &stmt, nullptr);
	if (result == SQLITE_OK)
	{
		while (sqlite3_step(stmt) == SQLITE_ROW)
		{
			int id = sqlite3_column_int(stmt, 0);
			std::string accStr = (char *)sqlite3_column_text(stmt, 9);
			std::vector<PointD> points;
			getGeometryPoints(points, stmt, 9);
			for (size_t i = 0; i < points.size(); i++)
			{
				std::cout << points[i].x << points[i].y << std::endl;
			}
		}
	}
	sqlite3_finalize(stmt);
	sqlite3_close_v2(db);
}

// 测试sqlite数据库连接是否成功
void testConnectDB(const char* path)
{
	sqlite3* sql = NULL; // 一个打开的数据库实例
	int result = sqlite3_open_v2(path, &sql, SQLITE_OPEN_READWRITE | SQLITE_OPEN_CREATE | SQLITE_OPEN_NOMUTEX | SQLITE_OPEN_SHAREDCACHE, NULL);
	if (result == SQLITE_OK) {
		std::cout << "打开数据库连接成功" << endl;;
	}
	else {
		std::cout << "打开数据库连接失败" << endl;
	}
}

int main()
{
	//testConnectDB("D:\\spatialite\\success\\test.db");
	readDbData("D:\\visual studio 2015\\Projects\\sqliteDemo1\\db.sqlite");
	system("pause");
    return 0;
}


```



生成解决方案会提示少`liblzma.dll`，在https://www.dllme.com/dll/files/liblzma_dll.html下载并放在`C://Windows/System32/`里就ok了。



# 3. 编译生成32位



向从OSGeo4W工具里下载各个依赖库x86版本的，发现OSGeo4W工具只支持64位。



尝试手动编译32位的依赖库。。

- freexl：https://www.gaia-gis.it/fossil/freexl/home

~~~
freexl里makefile.vc不能找到32位的选项
~~~

