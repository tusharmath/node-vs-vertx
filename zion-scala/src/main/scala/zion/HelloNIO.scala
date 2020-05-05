package zion

import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.{
  SelectionKey,
  Selector,
  ServerSocketChannel,
  SocketChannel
}
object HelloNIO {
  val PORT                     = 8090
  private var data: ByteBuffer = null

  def main(args: Array[String]): Unit = {
    val sel = Selector.open

    val address = new InetSocketAddress(PORT)

    val sch = ServerSocketChannel.open()
    sch.configureBlocking(false)
    sch.socket.bind(address)

    sch.register(sel, SelectionKey.OP_ACCEPT)
    new Thread(new SocketProcessor(sel)).start()
  }

  class SocketProcessor(val sel: Selector) extends Runnable {
    override def run(): Unit = {
      val readBuffer = ByteBuffer.allocateDirect(2048)
      val duplicate  = data.duplicate
      while (this.sel.select > 0) {
        val keys = this.sel.selectedKeys
        val i    = keys.iterator
        while (i.hasNext) {
          val key = i.next
          if (key.isAcceptable) accept(key)
          else if (key.isReadable) read(readBuffer, key)
          else if (key.isWritable) write(duplicate, key)
          i.remove()
        }
      }
    }

    private def accept(key: SelectionKey): Unit = {
      val sch = key.channel.asInstanceOf[ServerSocketChannel]
      val ch  = sch.accept
      ch.configureBlocking(false)
      ch.register(this.sel, SelectionKey.OP_READ)
    }

    private def write(duplicate: ByteBuffer, key: SelectionKey): Unit = {
      val ch = key.channel.asInstanceOf[SocketChannel]
      ch.write(duplicate)
      duplicate.rewind
      ch.register(this.sel, SelectionKey.OP_READ)
    }

    private def read(readBuffer: ByteBuffer, key: SelectionKey): Unit = {
      val ch = key.channel.asInstanceOf[SocketChannel]
      ch.read(readBuffer)
      readBuffer.rewind
      ch.register(this.sel, SelectionKey.OP_WRITE)
    }
  }

}
