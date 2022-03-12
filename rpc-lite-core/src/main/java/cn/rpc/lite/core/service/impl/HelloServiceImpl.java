package cn.rpc.lite.core.service.impl;

import cn.rpc.lite.core.innotation.RpcService;
import cn.rpc.lite.core.service.HelloService;

@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService {

    @Override
    public String sayHello(String name) {
        return "Hello! " + name;
    }
}
