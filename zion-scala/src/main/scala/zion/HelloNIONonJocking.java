package zion;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class HelloNIONonJocking {

    public static final int PORT = 8090;

    private final static ByteBuffer data;

    static {

        byte[] responseBody = new byte[1];

        byte[] response = ("HTTP/1.1 200 OK\r\n" +
                "Connection: Keep-Alive\r\n" +
                "Content-Type: text/plain\r\n" +
                "Content-Length: " + responseBody.length + "\r\n\r\n" +
                new String(responseBody)).getBytes();

        data = ByteBuffer.allocateDirect(response.length);
        data.put(response);
        data.flip();

    }

    public static void main(String[] args) throws Exception {

        ServerSocketChannel sch;
        Selector sel;

        InetSocketAddress addr = new InetSocketAddress(PORT);
        sch = ServerSocketChannel.open();
        sch.configureBlocking(false);
        sch.socket().bind(addr);

        sel = Selector.open();
        sch.register(sel, SelectionKey.OP_ACCEPT);

        new Thread(new SocketProcessor(sel)).start();

    }

    private static class SocketProcessor implements Runnable {

        private Selector sel;

        private SocketProcessor(Selector sel) {
            this.sel = sel;
        }

        @Override
        public void run() {
            try {

                ByteBuffer readBuffer = ByteBuffer.allocateDirect(2048);

                ByteBuffer duplicate = data.duplicate();

                while (this.sel.select() > 0) {
                    Set keys = this.sel.selectedKeys();
                    Iterator i = keys.iterator();
                    while (i.hasNext()) {
                        SelectionKey key = (SelectionKey) i.next();
                        if (key.isAcceptable()) {
                            accept(key);
                        } else if (key.isReadable()) {
                            read(readBuffer, key);
                        } else if (key.isWritable()) {
                            write(duplicate, key);
                        }
                        i.remove();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void accept(SelectionKey key) throws IOException {
            try {
                ServerSocketChannel sch = (ServerSocketChannel) key.channel();
                SocketChannel ch = sch.accept();
                ch.configureBlocking(false);
                ch.register(this.sel, SelectionKey.OP_READ);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void write(ByteBuffer duplicate, SelectionKey key) throws IOException {
            SocketChannel ch = (SocketChannel) key.channel();
            try {
                ch.write(duplicate);
                duplicate.rewind();
                ch.register(this.sel, SelectionKey.OP_READ);
            } catch (Exception e) {
                e.printStackTrace();
                ch.register(this.sel, 0);
            }
        }

        private void read(ByteBuffer readBuffer, SelectionKey key) throws IOException {
            SocketChannel ch = (SocketChannel) key.channel();
            try {
                ch.read(readBuffer);
                readBuffer.rewind();
                ch.register(this.sel, SelectionKey.OP_WRITE);
            } catch (Exception e) {
                e.printStackTrace();
                ch.register(this.sel, 0);
            }
        }

    }

}