package zion

import java.net.{InetSocketAddress, SocketOption, StandardSocketOptions}
import java.nio.ByteBuffer
import java.nio.channels.{
  AsynchronousChannelGroup,
  AsynchronousServerSocketChannel,
  AsynchronousSocketChannel,
  CompletionHandler
}
import java.util.concurrent.{Executors, TimeUnit}

object HelloAsyncNIO
    extends CompletionHandler[AsynchronousSocketChannel, Unit] {
  private val group =
    AsynchronousChannelGroup.withThreadPool(Executors.newSingleThreadExecutor())

  private val serverSocketChannel = AsynchronousServerSocketChannel.open(group)
  private val PORT                = 8081
  private val response            = HelloResponse.ok("HelloNIO.scala\n").getBytes()
  private val writeByteBuffer     = ByteBuffer.wrap(response)
  private val readByteBuffer      = ByteBuffer.allocateDirect(1024 * 2)

  val writeHandler = new CompletionHandler[Integer, AsynchronousSocketChannel] {
    override def completed(
        result: Integer,
        attachment: AsynchronousSocketChannel
    ): Unit = {
      attachment.close()
    }
    override def failed(
        exc: Throwable,
        attachment: AsynchronousSocketChannel
    ): Unit = {
      attachment.close()
    }
  }

  override def completed(
      socketChannel: AsynchronousSocketChannel,
      attachment: Unit
  ): Unit = {
    serverSocketChannel.accept((), this)

    socketChannel.setOption(
      StandardSocketOptions.SO_KEEPALIVE.asInstanceOf[SocketOption[Any]],
      true
    )

    socketChannel.write(
      writeByteBuffer.duplicate(),
      socketChannel,
      writeHandler
    )
  }

  override def failed(exc: Throwable, attachment: Unit): Unit = {
    exc.printStackTrace()
  }

  def main(args: Array[String]): Unit = {
    val address = new InetSocketAddress(PORT)
    serverSocketChannel.bind(address)

    serverSocketChannel.accept((), this)

    // Making the main thread wait
    group.awaitTermination(Long.MaxValue, TimeUnit.SECONDS)
  }
}
