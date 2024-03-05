# 毕业设计-基于Spring Cloud的音乐社交平台

## 1.项目介绍

本项目是一个基于微服务架构的前后端分离音乐社交平台，按照业务主要分为音乐模块、动态模块、用户模块和管理模块四个模块。采用微服务的架构，基于功能划分服务，`网关服务`、`音乐服务`、`动态服务`、`用户服务`、`检索服务`、`管理服务`、和`第三方服务`七个微服务。

## 2.项目技术栈

| 名称                 | 版本       |
| -------------------- | ---------- |
| **基础环境**         |            |
| JDK                  | 11         |
| **核心框架**         |            |
| Spring Boot          | 2.6.3      |
| Spring Cloud         | 2021.0.1   |
| Spring Cloud Alibaba | 2021.0.1.0 |
| Spring Security      | 2.6.11     |
| MyBatis              | 2.2.2      |
| MyBatis-Plus         | 3.5.2      |
| Redisson             | 3.20.0     |
| **微服务**           |            |
| Dubbo                | 2.7.15     |
| Spring Cloud Gateway | 3.1.1      |
| Nacos                | 2021.0.1.0 |
| Loadbalancer         | 3.1.1      |
| **中间件**           |            |
| MySQL                | 8.0.30     |
| Druid                | 1.2.15     |
| Redis                | 2.6.11     |
| RabbitMQ             | 2.6.3      |
| ElasticSearch        | 7.17.3     |
| **第三方工具**       |            |
| commons-lang3        | 3.12.0     |
| hutool-all           | 5.8.21     |
| lombok               | 1.18.24    |



## 3.项目架构图

![image-20240119105158798](https://imagebed-xuhuaiang.oss-cn-shanghai.aliyuncs.com/typora/image-20240119105158798.png)

