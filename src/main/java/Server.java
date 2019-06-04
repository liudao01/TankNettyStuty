import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * @author liuml
 * @explain
 * @time 2019-06-04 14:00
 */
public class Server {

    //    通道组
    public static ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public static void main(String[] args) {
        //线程池
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);//nio 的线程池
        EventLoopGroup workerGroup = new NioEventLoopGroup(2);//用于工作的

        //服务的bootstrap
        ServerBootstrap bootstrap = new ServerBootstrap();

        try {
            ChannelFuture future = bootstrap.group(bossGroup, workerGroup)// 绑定线程池
                .channel(NioServerSocketChannel.class)//指定使用的channel
                .childHandler(new ChannelInitializer<SocketChannel>() {// 绑定客户端连接时候触发操作
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new ServerChildHandler());//// 客户端触发操作
                    }
                })
                .bind(8888)
                .sync();// 服务器异步创建绑定
            System.out.println("server started");

            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if(!future.isSuccess()) {
                        System.out.println("not connected!");
                    } else {
                        System.out.println("connected!");
                    }
                }
            });

            //结束后关闭 关闭服务器通道
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //结束  shutdown  我的五杀被终结 哈哈
            workerGroup.shutdownGracefully();//// 释放线程池资源
            bossGroup.shutdownGracefully();
        }


    }


}

class ServerChildHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //channelActive 通道活动 创建的时候被调用
        Server.clients.add(ctx.channel());//假如有多个客户端链接那么 需要把新接入的客户端加入通道组

        //Successful connection
        ByteBuf buf = Unpooled.copiedBuffer("server : welcome  ".getBytes());
        Server.clients.writeAndFlush(buf);

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //通道里读数据
//        System.out.println("get client data");
        ByteBuf buf = null;

        buf = (ByteBuf)msg;
        byte[] bytes = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), bytes);
        System.out.println("server " + new String(bytes));
        Server.clients.writeAndFlush(buf);
    }
}
