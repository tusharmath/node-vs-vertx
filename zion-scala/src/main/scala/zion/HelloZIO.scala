package zion

import java.io.IOException
import java.net.{InetSocketAddress, SocketAddress}
import java.nio.{ByteBuffer, channels => jChannnels}
import java.util.concurrent.Executors

import zio._
import zio.stream.ZStream

object HelloZIO extends App {
  private val group = jChannnels.AsynchronousChannelGroup.withThreadPool(
    Executors.newSingleThreadExecutor()
  )
  private val serverSocketChannel = new AsynchronousServerSocketChannel(
    jChannnels.AsynchronousServerSocketChannel.open(group)
  )

  private val PORT            = 8081
  private val response        = HelloResponse.ok("ZIOServer\n").getBytes()
  private val writeByteBuffer = ByteBuffer.wrap(response)
  private val readByteBuffer  = ByteBuffer.allocateDirect(1024 * 2) // 2kb

  class AsynchronousSocketChannel(
      socketChannel: jChannnels.AsynchronousSocketChannel
  ) {
    def read(byteBuffer: ByteBuffer): Task[Int] =
      ZIO
        .effectAsyncWithCompletionHandler[Integer](handler =>
          socketChannel.read(byteBuffer.duplicate(), (), handler)
        )
        .map(_.toInt)

    def write(byteBuffer: ByteBuffer): Task[Int] =
      ZIO
        .effectAsyncWithCompletionHandler[Integer](handler =>
          socketChannel.write(byteBuffer.duplicate(), (), handler)
        )
        .map(_.toInt)

    def close(): Task[Unit] = Task { socketChannel.close() }
  }

  class AsynchronousServerSocketChannel(
      socketChannel: jChannnels.AsynchronousServerSocketChannel
  ) {
    def bind(address: SocketAddress): Task[AsynchronousServerSocketChannel] =
      Task {
        socketChannel.bind(address)
        this
      }

    def accept(): Task[AsynchronousSocketChannel] =
      ZIO
        .effectAsyncWithCompletionHandler[jChannnels.AsynchronousSocketChannel](
          handler => socketChannel.accept((), handler)
        )
        .map(new AsynchronousSocketChannel(_))
  }

  def socketHandler(socket: AsynchronousSocketChannel): Task[Unit] =
    for {
      byte <- socket.read(readByteBuffer).catchSome {
               case _: IOException => socket.close().as(0)
             }
      _ <- byte match {
            case -1 => socket.close()
            case 0  => UIO.unit
            case _ =>
              socket.write(writeByteBuffer).catchSome {
                case _: IOException => socket.close()
              } *> socketHandler(socket)
          }
    } yield ()

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    (for {
      server <- serverSocketChannel.bind(new InetSocketAddress(PORT))
      _ <- ZStream
            .repeatEffect(server.accept())
            // Allow only a million parallel connections
            .mapMParUnordered(1_000_000)(socketHandler)
            .runDrain
    } yield 0).orDie.as(0)
}
