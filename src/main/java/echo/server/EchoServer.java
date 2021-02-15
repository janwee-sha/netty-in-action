package echo.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

public class EchoServer {
    private final int port;

    public EchoServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
       /* if (args.length != 1) {
            System.err.println("Usage: " + EchoServer.class.getSimpleName() + " ");
            return;
        }*/
        int port = /*Integer.parseInt(args[0])*/9091;
        new EchoServer(port).start();

    }

    public void start() throws Exception {
        final EchoServerHandler serverHandler = new EchoServerHandler();
        EventLoopGroup group = new NioEventLoopGroup();//创建Event-LoopGroup
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(group)
                    .channel(NioServerSocketChannel.class)//指定所使用的NIO传输Channel
                    .localAddress(new InetSocketAddress(port))//使用指定的端口设置套接字地址
                    .childHandler(new ChannelInitializer() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            channel.pipeline().addLast(serverHandler);
                        }
                    });//添加一个EchoServer-Handler到子Channel的ChannelPipeline
            ChannelFuture future = b.bind().sync();//异步地绑定服务器
            future.channel().closeFuture().sync();//获取Channel地CloseFuture，并阻塞当前线程直到它完成
        } finally {
            group.shutdownGracefully().sync();//关闭EventLoopGroup，释放所有资源
        }
    }
}
