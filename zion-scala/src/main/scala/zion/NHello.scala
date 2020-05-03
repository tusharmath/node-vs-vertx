package zion

import java.net._
import java.nio._
import java.nio.channels._

object NHello extends App {
  val port   = 8080
  val server = AsynchronousServerSocketChannel.open()

  val address = new InetSocketAddress(port)
  val channel = server.bind(address)
  println("Starting...: " + port)

  def httpResponse(socket: AsynchronousSocketChannel)(content: String) = {

    val DEMLIMITER = "\r\n"
    val bytes = List(
      "HTTP/1.1 200 OK",
      "Content-Type: text/html",
      s"Content-Length: ${content.length()}",
      DEMLIMITER + content
    ).mkString(DEMLIMITER).getBytes()

    socket.write(ByteBuffer.wrap(bytes))
  }

  def itar: Unit = server.accept(
    (),
    new CompletionHandler[AsynchronousSocketChannel, Unit] {
      override def completed(
          socket: AsynchronousSocketChannel,
          x$2: Unit
      ): Unit = {
        httpResponse(socket)("Hello World\n")
        socket.close()
        itar
      }

      override def failed(x$1: Throwable, x$2: Unit): Unit = {
        throw x$1
      }

    }
  )

  sys.addShutdownHook {
    println("Stopping...")
    server.close()
  }

  itar

  while (true) {
    Thread.sleep(Int.MaxValue);
  }
  println("DONE")

}
