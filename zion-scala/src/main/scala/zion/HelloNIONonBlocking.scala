package zion

import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.{
  SelectionKey,
  Selector,
  ServerSocketChannel,
  SocketChannel
}

object HelloNIONonBlocking {
  val response: Array[Byte]       = HelloResponse.ok("Hello NIO World\n").getBytes()
  val readByteBuffer: ByteBuffer  = ByteBuffer.allocateDirect(1024)
  val writeByteBuffer: ByteBuffer = ByteBuffer.wrap(response)
  val PORT: Int                   = 8090

  def main(args: Array[String]): Unit = {
    val address             = new InetSocketAddress(PORT)
    val serverSocketChannel = ServerSocketChannel.open()
    serverSocketChannel.configureBlocking(false)
    serverSocketChannel.socket.bind(address)
    println(s"Starting...: ${PORT}")

    val selector = Selector.open()
    serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT)
    new Thread(new SocketProcessor(selector)).start()

    class SocketProcessor(var sel: Selector) extends Runnable {
      override def run(): Unit = {

        while (this.sel.select > 0) {
          val keys = this.sel.selectedKeys
          val i    = keys.iterator
          while (i.hasNext) {
            val key = i.next
            key match {
              case _ if key.isAcceptable =>
                val sch = key.channel.asInstanceOf[ServerSocketChannel]
                val ch  = sch.accept
                ch.configureBlocking(false)
                ch.register(this.sel, SelectionKey.OP_READ)
              case _ if key.isReadable =>
                val ch = key.channel.asInstanceOf[SocketChannel]
                ch.read(readByteBuffer)
                readByteBuffer.rewind
                ch.register(this.sel, SelectionKey.OP_WRITE)
              case _ if key.isWritable =>
                val ch        = key.channel.asInstanceOf[SocketChannel]
                val duplicate = writeByteBuffer.duplicate()
                ch.write(duplicate)
                duplicate.rewind
                ch.register(this.sel, SelectionKey.OP_READ)
            }
          }
          i.remove()
        }
      }
    }
  }
}
