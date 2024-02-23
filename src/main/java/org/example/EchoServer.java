package org.example;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Tag(name = "EchoServer", description = "Возвращает отправленное сообщение")
public class EchoServer {
    public static final Charset CHARSET = Charset.forName(System.getProperty("file.encoding"));

    public static final int PORT = 9000;
    private static final ExecutorService pool = Executors.newCachedThreadPool();

    @Operation(summary = "Стартует сервер")
    public void start() throws IOException {
        try(ServerSocketChannel socket = ServerSocketChannel.open(); Selector selector = Selector.open()) {
            socket.configureBlocking(false);
            socket.socket().bind(new InetSocketAddress(PORT));
            socket.register(selector, SelectionKey.OP_ACCEPT);
            while (true) {
                selector.select();
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();
                    if (key.isAcceptable()) {
                        SocketChannel channel = socket.accept();
                        System.out.println("Accepted connection from:" + channel.socket());
                        channel.configureBlocking(false);
                        pool.submit(new Task(channel));
                    }
                }
            }
        }
    }
}
