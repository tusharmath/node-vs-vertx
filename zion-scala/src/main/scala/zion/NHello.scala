package zion

import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels._

object NHello {

  val writeByteBuffer: ByteBuffer =
    ByteBuffer.wrap(HelloResponse.ok("Hello NIO\n").getBytes())

  class Writer(selector: Selector) extends Runnable {
    def run(): Unit = {
      while (true) {
        writeToChannel()
      }
    }

    private def writeToChannel(): Unit = {
      selector.select()
      val keys     = selector.selectedKeys()
      val iterator = keys.iterator()
      while (iterator.hasNext) {
        val key = iterator.next()
        if (key.isWritable) {
          val channel = key.channel().asInstanceOf[SocketChannel]
          channel.write(writeByteBuffer.duplicate())
          channel.close()
          println("WRITTEN")
        }
      }
    }
  }

  class Acceptor(accept: Selector, writer: IndexedSeq[Selector])
      extends Runnable {
    def run(): Unit = {
      var id = 0
      while (true) {
        accept.select()
        val iterator = accept.selectedKeys().iterator()

        while (iterator.hasNext) {
          val key = iterator.next()
          if (key.isAcceptable) {
            val channel =
              key.channel().asInstanceOf[ServerSocketChannel].accept()
            if (channel != null) {
              channel.configureBlocking(false)
              id = (id + 1) % writer.size

              channel.register(
                writer(id),
                SelectionKey.OP_WRITE
              )

              writer(id).wakeup()
            }
          }
        }
      }
    }
  }

  def main(args: Array[String]): Unit = {
    val port    = 8080
    val address = new InetSocketAddress(port)

    // Server Socket Channel
    val serverSocketChannel =
      ServerSocketChannel.open().bind(address).configureBlocking(false)

    // Connection Selector 0
    val connectionSelector = Selector.open()
    serverSocketChannel.register(connectionSelector, SelectionKey.OP_ACCEPT)

    // Writer Selector

    val writerSelector0 = Selector.open()
    val writerSelector1 = Selector.open()
    val writerSelector2 = Selector.open()
    val writerSelector3 = Selector.open()
    val writerSelector4 = Selector.open()

    new Thread(
      new Acceptor(
        connectionSelector,
        IndexedSeq(
          writerSelector0,
          writerSelector1,
          writerSelector2,
          writerSelector3,
          writerSelector4
        )
      )
    ).start()
    new Thread(new Writer(writerSelector0)).start()
    new Thread(new Writer(writerSelector1)).start()
    new Thread(new Writer(writerSelector2)).start()
    new Thread(new Writer(writerSelector3)).start()
    new Thread(new Writer(writerSelector4)).start()

    sys.addShutdownHook({
      println("Shutting down...")
      serverSocketChannel.close()
    })
  }
}
