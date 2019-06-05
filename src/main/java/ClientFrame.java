import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author liuml
 * @explain
 * @time 2019-06-04 15:02
 */
public class ClientFrame extends Frame {

    public TextArea mTextArea = new TextArea();
    ;
    public TextField mTextField = new TextField();
    private static ClientFrame sClientFrame;
    private static Channel sChannel;

    public ClientFrame() {

        this.setSize(600, 400);
        this.setLocation(100, 200);
        //BorderLayout布置容器的边框布局,它可以对容器组件进行安排,并调整其大小,使其符合下列五个区域:北、南、东、西、中,
        this.add(mTextArea, BorderLayout.CENTER);
        this.add(mTextField, BorderLayout.SOUTH);
        mTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMsg(mTextField.getText());
                mTextField.setText(" ");

            }
        });
        ClientFrameHandler.getInstance().setReadMsgInterface(new ClientFrameHandler.ReadMsgInterface() {
            @Override
            public void readMsg(String string) {
                mTextArea.append(string + "\n");
            }
        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosed(e);
                System.exit(0);
            }
        });
        this.setVisible(true);
    }

    public static void sendMsg(String msg) {
        System.out.println("发送消息" + msg);
        ByteBuf buf = Unpooled.copiedBuffer(msg.getBytes());
        sChannel.writeAndFlush(buf);
    }

    public static void main(String[] args) {
        sClientFrame = new ClientFrame();
        //线程池
        EventLoopGroup group = new NioEventLoopGroup(1);//nio 的线程池
        //当需要引导客户端或一些无连接协议时，需要使用Bootstrap类,创建一个新的 Bootstrap 来创建和连接到新的客户端管道
        Bootstrap bootstrap = new Bootstrap();
        try {
            //ChannelFuture 异步通知  Netty提供了ChannelFuture接口，
            // 其addListener()方法注册了一个ChannelFutureListener，以便在某个操作完成时（无论是否成功）得到通知。
            //ChannelFuture 是个观察者
            ChannelFuture channelFuture = bootstrap.group(group)
                .channel(NioSocketChannel.class)//指定连接到服务器的channel 的类型是nio的
                .handler(new ClientFrameChannelInitializer())//handler 的意思 发生事件的时候交给ClientChannelInitializer 处理
                .connect("localhost", 8888);

            channelFuture.sync();// 服务器异步创建绑定
            System.out.println("client started");
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (!future.isSuccess()) {
                        System.out.println("not connected!");
                    } else {
                        sChannel = channelFuture.channel();
                        System.out.println("connected!");
                    }
                }
            });
            //服务器同步连接断开时,这句代码执行
            channelFuture.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}

//ChannelInitializer 它提供了一种简单的方法，可以在通道注册到其EventLoop后对其进行初始化。
class ClientFrameChannelInitializer extends ChannelInitializer {

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline().addLast(ClientFrameHandler.getInstance());
    }
}

//ChannelInboundHandler的一个简单实现，默认情况下不会做任何处理，
// 只是简单的将操作通过fire*方法传递到ChannelPipeline中的下一个ChannelHandler中让链中的下一个ChannelHandler去处理。
class ClientFrameHandler extends ChannelInboundHandlerAdapter {


    static ClientFrameHandler mClientFrameHandler = new ClientFrameHandler();

    public static ClientFrameHandler getInstance() {
        return mClientFrameHandler;
    }

    interface ReadMsgInterface {
        void readMsg(String string);
    }

    ReadMsgInterface mReadMsgInterface;

    public void setReadMsgInterface(ReadMsgInterface readMsgInterface) {
        mReadMsgInterface = readMsgInterface;
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx);
        //第一次链接发个消息 channle 第一次连上可用，写出一个字符串
        ByteBuf buf = Unpooled.copiedBuffer("link start".getBytes());
        ctx.writeAndFlush(buf);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //获取从服务端返回的数据
        ByteBuf buf = null;

        buf = (ByteBuf)msg;
        byte[] bytes = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), bytes);
        String s = new String(bytes);
        System.out.println("获得消息: " + s);
        if (mReadMsgInterface != null) {
            mReadMsgInterface.readMsg(s);
        }
    }


}