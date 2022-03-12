package cn.rpc.lite.client;

import cn.rpc.lite.core.codec.RpcDecoder;
import cn.rpc.lite.core.codec.RpcEncoder;
import cn.rpc.lite.core.pojo.RpcRequest;
import cn.rpc.lite.core.pojo.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rpc客户端，兼顾发送请求以及处理响应
 */
public class RpcClient extends SimpleChannelInboundHandler<RpcResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcClient.class);

    private String host;

    private int port;

    private RpcResponse response; // 保存服务端的响应结果

    private final Object obj = new Object(); // 锁对象

    public RpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * 处理服务端的响应
     * @param channelHandlerContext
     * @param response
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse response) throws Exception {
        this.response = response;

        synchronized (obj) {
            obj.notifyAll();
        }
    }

    public RpcResponse send(RpcRequest request) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                     .channel(NioSocketChannel.class)
                     .handler(new ChannelInitializer<SocketChannel>() {
                         @Override
                         protected void initChannel(SocketChannel channel) throws Exception {
                             channel.pipeline().addLast(new RpcDecoder(RpcResponse.class)) // 解码器
                                              .addLast(new RpcEncoder(RpcRequest.class))   // 编码器
                                              .addLast(RpcClient.this); // 客户端本身处理服务端响应
                         }
                     }).option(ChannelOption.SO_KEEPALIVE, true);

            // 1. 连接服务端
            ChannelFuture future = bootstrap.connect(host, port).sync();
            // 2. 发送请求
            future.channel().writeAndFlush(request).sync();

            // 3. 进入等待状态，等待服务端响应后保存响应结果并唤醒
            synchronized (obj) {
                obj.wait();
            }

            // 4. 如果收到响应结果，则关闭future
            if (response != null) {
                future.channel().closeFuture().sync();
            }
            return response;
        } finally {
            group.shutdownGracefully();
        }
    }

}
