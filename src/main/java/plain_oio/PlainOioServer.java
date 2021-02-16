package plain_oio;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PlainOioServer {
    private int count = 0;

    public static void main(String[] args) throws IOException {
        new PlainOioServer().server(9091);
    }

    public void server(int port) throws IOException {
        final Executor executor = Executors.newFixedThreadPool(100);
        final ServerSocket socket = new ServerSocket(port);//将服务器绑定到指定端口
        try {
            while (true) {
                final Socket cSocket = socket.accept();//接受连接
                count++;
                System.out.println("Accepted connection " + count + " from " + cSocket);
                executor.execute(() -> {
                    try {
                        while (count == 1) {
                            TimeUnit.MILLISECONDS.sleep(10000);
                        }
                        OutputStream out;
                        out = cSocket.getOutputStream();
                        out.write("Hi!\r\n".getBytes(StandardCharsets.UTF_8));
                        out.flush();
                        cSocket.close();
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            cSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
