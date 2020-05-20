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

/**
  * This is potentially as fast as it can get using NIO APIs.
  */
object HelloAsyncNIO {
  // Create a thread pool for the socket channel
  // It would be better to have probably only one thread for events.
  // That pool could be shared betwee the SocketServer and in future SocketClients.
  private val group =
    AsynchronousChannelGroup.withThreadPool(Executors.newFixedThreadPool(24))

  // Socket to accept connections
  private val serverSocketChannel = AsynchronousServerSocketChannel.open(group)

  // Port to be used to connect
  private val PORT = 8081

  // Flag to handle logging
  private val ENABLE_LOGGING = false

  /**
    * Contains utilities to manage read/write on the socket channels
    */
  object NIOBuffer {
    private val response        = HelloResponse.ok("HelloAsyncNIO.scala\n").getBytes()
    private val writeByteBuffer = ByteBuffer.wrap(response)
    private val readByteBuffer  = ByteBuffer.allocateDirect(1024 * 2) // 2kb
    def read(
        socket: AsynchronousSocketChannel
    )(h: CompletionHandler[Integer, AsynchronousSocketChannel]): Unit =
      socket.read(readByteBuffer.duplicate(), socket, h)
    def write(
        socket: AsynchronousSocketChannel
    )(h: CompletionHandler[Integer, AsynchronousSocketChannel]): Unit =
      socket.write(writeByteBuffer.duplicate(), socket, h)
  }

  // Generic async completion handler
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

  // Logging utility
  def log(input: Any*): Unit = {
    if (ENABLE_LOGGING) println(input.map(_.toString).mkString(", "))
  }

  private val onAccept
      : Handle[AsynchronousSocketChannel, AsynchronousServerSocketChannel] =
    Handle[AsynchronousSocketChannel, AsynchronousServerSocketChannel](
      (socket, server) => {
        log("\nACCEPT")

        // Accept new connections immediately
        server.accept(serverSocketChannel, onAccept)

        // Read from the current socket
        NIOBuffer.read(socket)(onRead)
      }
    )

  private val onRead: Handle[Integer, AsynchronousSocketChannel] =
    Handle[Integer, AsynchronousSocketChannel]((bytes, socket) => {
      log("READ", bytes)

      // EOF, meaning connection can be closed
      if (bytes == -1) socket.close()

      // Some data was read and now we can respond back
      else if (bytes > 0) NIOBuffer.write(socket)(onWrite)

    })

  private val onWrite: Handle[Integer, AsynchronousSocketChannel] =
    Handle[Integer, AsynchronousSocketChannel]((bytes, socket) => {
      log("WRITE", bytes)

      // Read from the socket
      NIOBuffer.read(socket)(onRead)
    })

  def main(args: Array[String]): Unit = {

    // Setup socket channel
    serverSocketChannel.bind(new InetSocketAddress(PORT))
    serverSocketChannel.accept(serverSocketChannel, onAccept)

    // Making the main thread wait
    group.awaitTermination(Long.MaxValue, TimeUnit.SECONDS)
  }
}
