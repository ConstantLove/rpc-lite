# 轻量级RPC框架

## 涉及技术以及选择理由：
1）SpringBoot - 配置少，兼容各种第三方框架。

2）Netty - 包装了Java NIO，提供更易读易用的接口。

3）ProtoStuff - 常用序列化框架

4）ZooKeeper - 服务注册与服务发现

## Quick Start

`1. 运行服务端启动类发布服务 RpcBootstrap`

`2. 运行客户端单元测试类模拟请求 ClientApplicationTests.helloTest`
