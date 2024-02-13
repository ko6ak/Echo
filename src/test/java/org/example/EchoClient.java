package org.example;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

public class EchoClient {
    private final int count;

    public EchoClient(int count) {
        this.count = count;
    }

    public void start() throws IOException {
        SocketChannel keyChannel = null;
        ByteBuffer endBuffer = ByteBuffer.wrap(Task.STOP_WORD.getBytes());
        ByteBuffer buffer = ByteBuffer.allocate(Task.BUFFER_SIZE);
        Charset charset = Charset.forName(System.getProperty("file.encoding"));

        try(SocketChannel channel = SocketChannel.open(); Selector selector = Selector.open();) {
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress(InetAddress.getLocalHost(), EchoServer.PORT));
            channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE | SelectionKey.OP_CONNECT);

            boolean written = false;
            int i = 0;

            while (true) {
                selector.select();
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                if (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();
                    keyChannel = (SocketChannel) key.channel();

                    if (key.isConnectable() && !channel.isConnected()) channel.finishConnect();
                    if (key.isReadable() && written) {
                        if (keyChannel.read(buffer.clear()) > 0) {
                            written = false;
                            String response = charset.decode(buffer.flip()).toString();
                            System.out.println(response);
                            if (i == count) break;
                        }
                    }
                    if (key.isWritable() && !written) {
                        if (i < count) keyChannel.write(ByteBuffer.wrap(("echo " + i).getBytes()));
                        written = true;
                        i++;
                    }
                }
            }
            keyChannel.write(endBuffer);
        } finally {
            if (keyChannel != null) keyChannel.close();
        }
    }
}
