package cn.rpc.lite.client.configuration;

import cn.rpc.lite.client.discovery.ServiceDiscovery;
import cn.rpc.lite.client.proxy.RpcProxy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
public class ClientConfiguration {

    @Value("${registry.address}")
    private String registryAddress;

    private ServiceDiscovery serviceDiscovery;

    @Bean
    ServiceDiscovery serviceDiscovery() {
        this.serviceDiscovery = new ServiceDiscovery(registryAddress);
        return serviceDiscovery;
    }

    @Bean
    @DependsOn("serviceDiscovery")
    RpcProxy rpcProxy() {
        return new RpcProxy(serviceDiscovery);
    }

}
