package plain_nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class PlainNioServer {
    public static void main(String[] args) throws IOException {
        new PlainNioServer().server(9091);
    }

    public void server(int port) throws IOException {
        ServerSocketChannel sChannel = ServerSocketChannel.open();
        sChannel.configureBlocking(false);
        ServerSocket sSocket = sChannel.socket();
        InetSocketAddress addr = new InetSocketAddress(port);
        sSocket.bind(addr);//将服务器绑定到选定的端口
        Selector selector = Selector.open();//打开Selector来处理Channel
        sChannel.register(selector, SelectionKey.OP_ACCEPT);//将ServerSocket注册到Selector以接受连接
        final ByteBuffer msg = ByteBuffer.wrap("Hi!\r\n".getBytes());
        while (true) {
            try {
                selector.select();//等待需要处理的新事件；阻塞将一直持续到下一个换入事件
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
            Set<SelectionKey> readKeys = selector.selectedKeys();//获取所有接收事件的Selection-Key实例
            Iterator<SelectionKey> it = readKeys.iterator();
            while (it.hasNext()) {
                SelectionKey key = it.next();
                it.remove();
                try {
                    if (key.isAcceptable()) {//检查事件是否一个新的已经就绪可以被接受的连接
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        SocketChannel client = server.accept();
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ, msg.duplicate());//接受客户端，并将它注册到选择器
                        System.out.println("Accepted connection from " + client);
                    }
                    if (key.isWritable()) {//检查套接字是否准备好写数据
                        SocketChannel client = (SocketChannel) key.channel();
                        ByteBuffer buffer = (ByteBuffer) key.attachment();
                        while (buffer.hasRemaining()) {
                            if (client.write(buffer) == 0) {
                                break;
                            }
                        }
                        client.close();
                    }
                } catch (IOException e) {
                    key.cancel();
                    try {
                        key.channel().close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }
}
