package zion

import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.{
  AsynchronousChannelGroup,
  AsynchronousServerSocketChannel,
  AsynchronousSocketChannel,
  CompletionHandler
}
import java.util.concurrent.{Executors, TimeUnit}

object HelloAsyncNIO {
  private val ENABLE_LOGGING = false
  private val group =
    AsynchronousChannelGroup.withThreadPool(Executors.newFixedThreadPool(24))

  private val serverSocketChannel = AsynchronousServerSocketChannel.open(group)
  private val PORT                = 8081
  private val response            = HelloResponse.ok("HelloAsyncNIO.scala\n").getBytes()
  private val writeByteBuffer     = ByteBuffer.wrap(response)
  private val readByteBuffer      = ByteBuffer.allocateDirect(1024 * 2)

  case class Handle[V, A](cb: (V, A) => Unit) extends CompletionHandler[V, A] {
    override def completed(result: V, attachment: A): Unit =
      cb(result, attachment)
    override def failed(cause: Throwable, attachment: A): Unit = {
      cause match {
        case e: IOException => log(e.getMessage)
        case _              => cause.printStackTrace()
      }
    }
  }

  def log(input: Any*): Unit = {
    if (ENABLE_LOGGING) println(input.map(_.toString).mkString(", "))
  }

  private val accept
      : Handle[AsynchronousSocketChannel, AsynchronousServerSocketChannel] =
    Handle[AsynchronousSocketChannel, AsynchronousServerSocketChannel](
      (socket, server) => {
        log("\nACCEPT")
        server.accept(serverSocketChannel, accept)
        readFromSharedBuffer(socket)
      }
    )

  private def readFromSharedBuffer(socket: AsynchronousSocketChannel): Unit =
    socket.read(readByteBuffer.duplicate(), socket, read)

  private def writeToSharedBuffer(socket: AsynchronousSocketChannel): Unit =
    socket.write(writeByteBuffer.duplicate(), socket, write)

  private val read: Handle[Integer, AsynchronousSocketChannel] =
    Handle[Integer, AsynchronousSocketChannel]((bytes, socket) => {
      log("READ", bytes)

      // EOF
      if (bytes == -1) socket.close()

      // Some data was read and now we can respond back
      else if (bytes > 0)
        writeToSharedBuffer(socket)

    })
  private val write: Handle[Integer, AsynchronousSocketChannel] =
    Handle[Integer, AsynchronousSocketChannel]((bytes, socket) => {
      log("WRITE", bytes)
      readFromSharedBuffer(socket)
    })

  def main(args: Array[String]): Unit = {
    val address = new InetSocketAddress(PORT)
    serverSocketChannel.bind(address)
    serverSocketChannel.accept(serverSocketChannel, accept)

    // Making the main thread wait
    group.awaitTermination(Long.MaxValue, TimeUnit.SECONDS)
  }
}
