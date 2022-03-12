package cn.rpc.lite.client.proxy;

import cn.rpc.lite.client.RpcClient;
import cn.rpc.lite.client.discovery.ServiceDiscovery;
import cn.rpc.lite.core.pojo.RpcRequest;
import cn.rpc.lite.core.pojo.RpcResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * Rpc代理类，封装发送Rpc请求逻辑
 */
public class RpcProxy {

    private String serverAddress;

    private ServiceDiscovery serviceDiscovery;

    public RpcProxy(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public RpcProxy(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    public <T> T create(Class<?> interfaceClass) {
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        // 1. 准备Rpc请求数据
                        RpcRequest request = new RpcRequest();
                        request.setRequestId(UUID.randomUUID().toString());
                        request.setClassName(method.getDeclaringClass().getName());
                        request.setMethodName(method.getName());
                        request.setParameterTypes(method.getParameterTypes());
                        request.setParameters(args);

                        // 2. 找到Rpc服务器地址
                        if (serviceDiscovery != null) {
                            serverAddress = serviceDiscovery.discover();
                        }

                        String[] arr = serverAddress.split(":");
                        String host = arr[0];
                        int port = Integer.parseInt(arr[1]);

                        // 3. 发送请求
                        RpcClient client = new RpcClient(host, port);
                        RpcResponse response = client.send(request);

                        if (response.isError()) {
                            throw response.getError();
                        } else {
                            return response.getResult();
                        }
                    }
                }
        );
    }
}
