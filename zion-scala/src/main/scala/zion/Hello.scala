package zion


import zio._
import zio.nio.SocketAddress
import zio.nio.channels._
import zio.stream._

object Hello extends App {

  def write(socket: AsynchronousSocketChannel): IO[Exception, Int] = {
    socket.write(
      Chunk.fromArray(message)
    )
  }

  private lazy val message: Array[Byte] = {
    val rn      = "\r\n"
    val content = "Hello World\n"

    List(
      "HTTP/1.1 200 OK",
      "Content-Type: text/html",
      "Connection: keep-alive",
      "Keep-Alive: timeout=5, max=1000",
      s"Content-Length: ${content.length()}",
      rn + content
    ).mkString(rn).getBytes()
  }

  def handle(socket: AsynchronousSocketChannel) = write(socket)

  def run(args: List[String]) =
    AsynchronousServerSocketChannel()
      .use(server => {
        for {
          address <- SocketAddress.inetSocketAddress(8080)
          _       <- server.bind(address)
          _ <- ZStream
                .repeatEffect(server.accept.preallocate)
                .map(conn => ZStream.managed(conn.withEarlyRelease))
                .flatMapPar(12) { connectionStream =>
                  connectionStream.mapMPar(12)({
                    case (_, connection) => handle(connection)
                  })
                }
                //                .mapMParUnordered(2)(k => k.use(s => handle(s._2)).fork)
                .runDrain
        } yield 0
      })
      .orDie
}
