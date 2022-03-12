package cn.rpc.lite.server.configutation;

import cn.rpc.lite.server.RpcServer;
import cn.rpc.lite.server.registry.ServiceRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * 服务器自动配置，在启动时会自动创建Bean
 */
@Configuration
public class ServerConfiguration {

    @Value("${registry.address}")
    public String registryAddress;

    @Value("${server.address}")
    public String serverAddress;

    private ServiceRegistry ServiceRegistry;

    @Bean
    ServiceRegistry serviceRegistry() {
        ServiceRegistry = new ServiceRegistry(registryAddress);
        return ServiceRegistry;
    }

    /**
     * @DependsOn 设置依赖关系，RpcServer会在ServiceRegistry之后被加载进容器
     */
    @Bean
    @DependsOn("serviceRegistry")
    RpcServer rpcServer() {
        return new RpcServer(serverAddress, this.ServiceRegistry);
    }

}
