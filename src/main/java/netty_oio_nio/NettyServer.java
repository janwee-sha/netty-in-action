package netty_oio_nio;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.oio.OioServerSocketChannel;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class NettyServer {
    public static void main(String[] args) throws Exception {
//        new NettyServer().server(9091, true);
        new NettyServer().server(9091, false);
    }

    public void server(int port, boolean blocking) throws Exception {
        final ByteBuf buf = Unpooled.copiedBuffer("Hi!\r\n", StandardCharsets.UTF_8);
        EventLoopGroup group;
        if (blocking)
            group = new OioEventLoopGroup();//为阻塞模式使用OioEventLoopGroup
        else
            group = new NioEventLoopGroup();//为非阻塞模式使用NioEventLoopGroup
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(group)
                    .channel(blocking ? OioServerSocketChannel.class : NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    .childHandler(new ChannelInitializer() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            channel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    ctx.writeAndFlush(buf.duplicate())
                                            .addListener(ChannelFutureListener.CLOSE);
                                }
                            });
                        }
                    });
            ChannelFuture cFuture = b.bind().sync();
            cFuture.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }

    }
}
