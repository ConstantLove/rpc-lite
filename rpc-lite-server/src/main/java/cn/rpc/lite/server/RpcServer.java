package cn.rpc.lite.server;

import cn.rpc.lite.core.codec.RpcDecoder;
import cn.rpc.lite.core.codec.RpcEncoder;
import cn.rpc.lite.server.handler.RpcHandler;
import cn.rpc.lite.core.innotation.RpcService;
import cn.rpc.lite.core.pojo.RpcRequest;
import cn.rpc.lite.core.pojo.RpcResponse;
import cn.rpc.lite.server.registry.ServiceRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;

/**
 * Rpc服务类，在SpringBoot启动时会由ServerConfiguration传入serverAddress（本服务器地址）和serviceRegistry(服务注册类)创建Bean并放入容器
 */
public class RpcServer implements ApplicationContextAware, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);

    private String serverAddress;

    private ServiceRegistry serviceRegistry;

    private Map<String, Object> rpcServiceMap = new HashMap<>(); // Rpc服务缓存

    public RpcServer(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public RpcServer(String serverAddress, ServiceRegistry serviceRegistry) {
        this.serverAddress = serverAddress;
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * 在Bean被初始化时回调该方法，自动进行服务发布以及注册
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        EventLoopGroup boss = new NioEventLoopGroup();   // 监听线程组
        EventLoopGroup worker = new NioEventLoopGroup(); // 工作线程组
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new RpcDecoder(RpcRequest.class))  // 解码器
                                                    .addLast(new RpcEncoder(RpcResponse.class)) // 编码器
                                                    .addLast(new RpcHandler(rpcServiceMap));    // Rpc服务处理器
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            String[] arr = serverAddress.split(":");
            String host = arr[0];
            int port = Integer.parseInt(arr[1]);

            ChannelFuture future = bootstrap.bind(host, port).sync();
            LOGGER.debug("Server started on port {}", port);

            if (serviceRegistry != null) {
                serviceRegistry.register(serverAddress);
            }

            future.channel().closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    /**
     * Spring Aware回调注入ApplicationContext
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 1. 从ApplicationContext中获取所有Rpc服务对象
        Map<String, Object> rpcServiceBeans = applicationContext.getBeansWithAnnotation(RpcService.class);
        // 2. 如果存在Rpc服务，则保存接口名与对象的映射
        if (MapUtils.isNotEmpty(rpcServiceBeans)) {
            rpcServiceBeans.values().stream().forEach( bean -> {
                String interfaceName = bean.getClass().getAnnotation(RpcService.class).value().getName();
                rpcServiceMap.put(interfaceName, bean);
            });
        }
    }
}
