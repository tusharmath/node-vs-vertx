package zion

import java.net._
import java.nio.ByteBuffer
import java.nio.channels._

object NHello {

  val writeByteBuffer: ByteBuffer =
    ByteBuffer.wrap(HelloResponse.ok("Hello NIO\n").getBytes())

  def main(args: Array[String]): Unit = {
    val port    = 8080
    val address = new InetSocketAddress(port)

    // Server Socket Channel
    val serverSocketChannel = ServerSocketChannel.open()
    serverSocketChannel.bind(address)
    serverSocketChannel.configureBlocking(false)

    // Selector
    val selector = Selector.open()
    serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT)

    new Thread(new Live()).start()

    class Live extends Runnable {
      def run(): Unit = {
        while (true) {
          selector.select()
          val iterator = selector.selectedKeys().iterator()
          while (iterator.hasNext) {
            val key = iterator.next()
            key match {
              case _ if key.isAcceptable =>
                val channel =
                  key.channel().asInstanceOf[ServerSocketChannel].accept()
                if (channel != null) {
                  channel.configureBlocking(false)
                  channel.register(selector, SelectionKey.OP_WRITE)
                }
              case _ if key.isWritable =>
                val channel = key.channel().asInstanceOf[SocketChannel]
                channel.write(writeByteBuffer.duplicate())
                channel.close()
            }
          }
          iterator.remove()
        }
      }
    }

    sys.addShutdownHook({
      println("Shutting down...")
      serverSocketChannel.close()
    })

    println("Exiting...")
  }
}
