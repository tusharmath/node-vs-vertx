package zion

import zio._
import zio.duration._

import zio.nio.channels._
import zio.stream._
import zio.nio.SocketAddress

object Hello extends App {
  val DEMLIMITER = "\r\n"

  def write(socket: AsynchronousSocketChannel)(
      content: String
  ): IO[Exception, Int] = {
    socket.write(
      Chunk.fromArray(
        (Chunk(
          "HTTP/1.1 200 OK",
          "Content-Type: text/html",
          s"Content-Length: ${content.length()}",
          DEMLIMITER + content
        ).mkString(DEMLIMITER)).getBytes()
      )
    )
  }

  def handle(socket: AsynchronousSocketChannel) =
    for {
      _ <- write(socket)("Hello World\n")
    } yield ()

  def run(args: List[String]) =
    AsynchronousServerSocketChannel()
      .use(server => {
        for {
          addr <- SocketAddress.inetSocketAddress(8080)
          _    <- server.bind(addr)
          _ <- ZStream
                .repeatEffect(for {
                  channel <- server.accept.preallocate
                } yield channel)
                .mapMParUnordered(Int.MaxValue)(_.use(handle))
                .runDrain
        } yield 0
      })
      .orDie
}
