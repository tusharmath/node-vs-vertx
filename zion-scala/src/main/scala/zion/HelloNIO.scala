package zion

import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.{
  SelectionKey,
  Selector,
  ServerSocketChannel,
  SocketChannel
}

import scala.collection.mutable
import scala.concurrent.duration._

object HelloNIO {
  val response: Array[Byte]       = HelloResponse.ok("HelloNIO.scala\n").getBytes()
  val writeByteBuffer: ByteBuffer = ByteBuffer.wrap(response)
  val readByteBuffer: ByteBuffer  = ByteBuffer.allocateDirect(1024 * 2)
  val PORT                        = 8081
  val selector                    = Selector.open()
  val address                     = new InetSocketAddress(PORT)

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
        if (key.isAcceptable) accept(key)
        else if (key.isReadable) read(key)
        else if (key.isWritable) write(key)
      }
      iterator.remove()
    }
  }

  def accept(key: SelectionKey): Unit = {
    val serverSocketChannel = key.channel().asInstanceOf[ServerSocketChannel]
    val socketChannel       = serverSocketChannel.accept()
    if (socketChannel != null) {
      socketChannel.configureBlocking(false)
      socketChannel.register(selector, SelectionKey.OP_READ)
    }
  }

  def read(key: SelectionKey): Unit = {
    val socketChannel = key.channel().asInstanceOf[SocketChannel]
    tryWithSocket(socketChannel) {
      val bytes = socketChannel.read(readByteBuffer)
      readByteBuffer.rewind()

      if (bytes > -1) {
        key.interestOps(SelectionKey.OP_WRITE)
      } else {
        key.interestOps(0)
      }
    }
  }

  def write(key: SelectionKey): Unit = {
    val socketChannel = key.channel().asInstanceOf[SocketChannel]
    tryWithSocket(socketChannel) {
      socketChannel.write(writeByteBuffer.duplicate())

      socketChannel.register(selector, SelectionKey.OP_READ)
    }
  }

  def tryWithSocket(socketChannel: SocketChannel)(task: => Unit) = {
    try {
      task
    } catch {
      case e: Throwable =>
        e.printStackTrace()
        socketChannel.close()
    }
  }
}
