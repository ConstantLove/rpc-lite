package cn.rpc.lite.core.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * 解码器
 */
public class RpcDecoder extends ByteToMessageDecoder {

    private Class<?> clazz;

    public RpcDecoder(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        // 1. 如果当前可读字节小于4(int长度)，直接结束
        if (byteBuf.readableBytes() < 4) return;

        // 2. 保存当前读指针，无论buffer传递到哪里，都可以调用resetReaderIndex()恢复到保存时的状态
        byteBuf.markReaderIndex();

        // 3. 读取int类型的数据长度，如果长度小于0，关闭通道
        int dataLength = byteBuf.readInt();
        if (dataLength < 0) {
            channelHandlerContext.close();
        }

        // 4. 当前可读长度小于数据长度，重置索引并返回
        if (byteBuf.readableBytes() < dataLength) {
            byteBuf.resetReaderIndex();
            return;
        }

        // 5. 读取数据并反序列化出对象，将对象沿处理器链向下传递
        byte[] data = new byte[dataLength];
        byteBuf.readBytes(data);
        Object obj = RpcCodec.deserialize(data, clazz);
        list.add(obj);
    }

}
