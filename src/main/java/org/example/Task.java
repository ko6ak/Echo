package org.example;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class Task implements Runnable {
    public static final int BUFFER_SIZE = 1024;
    public static final String STOP_WORD = "END";

    private final SocketChannel channel;
    private final Selector selector;

    public Task(SocketChannel channel) throws IOException {
        this.channel = channel;
        selector = Selector.open();
    }

    public void run() {
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        boolean read = false, done = false;
        String response = null;
        try {
            channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            while (!done) {
                selector.select();
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();
                    if (key.isReadable() && !read) {
                        if (channel.read(buffer) > 0) read = true;
                        CharBuffer cb = EchoServer.CHARSET.decode(buffer.flip());
                        response = cb.toString();
                    }
                    if (key.isWritable() && read) {
                        channel.write(buffer.rewind());
                        if (response.equals(STOP_WORD)) done = true;
                        else System.out.println("Echoing: " + response);
                        buffer.clear();
                        read = false;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
