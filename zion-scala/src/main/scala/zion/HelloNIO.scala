package zion

import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.{
  SelectionKey,
  Selector,
  ServerSocketChannel,
  SocketChannel
}

object HelloNIO {
  val response: Array[Byte]       = HelloResponse.ok("ok\n").getBytes()
  val writeByteBuffer: ByteBuffer = ByteBuffer.wrap(response)
  val readByteBuffer: ByteBuffer  = ByteBuffer.allocateDirect(1024 * 2)
  val PORT: Int                   = 8081
  val selector: Selector          = Selector.open()
  val readWriteSelector: Selector = Selector.open()
  val address: InetSocketAddress  = new InetSocketAddress(PORT)

  def main(args: Array[String]): Unit = {
    val serverSocketChannel = ServerSocketChannel.open()

    serverSocketChannel.socket.bind(address)
    serverSocketChannel.configureBlocking(false)
    serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT)

    while (true) {
      selector.select()
      val keys     = selector.selectedKeys()
      val iterator = keys.iterator()
      while (iterator.hasNext) {
        val key = iterator.next()
        try {
          if (key.isAcceptable) accept(key)
          else if (key.isReadable) read(key)
          else if (key.isWritable) write(key)
        } catch {
          case e: IOException =>
            println(e.getMessage)
            key.cancel()
        }
      }
      iterator.remove()
    }
  }

  def accept(key: SelectionKey): Unit = {
    val serverSocketChannel = key.channel().asInstanceOf[ServerSocketChannel]
    val socketChannel       = serverSocketChannel.accept()

    if (socketChannel != null) {
      socketChannel.configureBlocking(false)
      socketChannel.socket().setKeepAlive(true)
      socketChannel.register(selector, SelectionKey.OP_READ)
    }

  }

  def read(key: SelectionKey): Unit = {
    val socketChannel = key.channel().asInstanceOf[SocketChannel]

    val count = socketChannel.read(readByteBuffer)
    if (count == -1) {
      key.interestOps(0)
    } else {
      key.interestOps(SelectionKey.OP_WRITE)
    }
  }

  def write(key: SelectionKey): Unit = {
    val socketChannel = key.channel().asInstanceOf[SocketChannel]
    socketChannel.write(writeByteBuffer)
    writeByteBuffer.rewind()
    key.interestOps(SelectionKey.OP_READ)
  }
}
