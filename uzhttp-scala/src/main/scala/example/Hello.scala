package example
import java.net.InetSocketAddress
import uzhttp.server.Server
import uzhttp.{Request, Response, RefineOps}
import uzhttp.websocket.Frame
import zio.{App, ZIO, Task}

object ExampleServer extends App {
  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    Server
      .builder(
        address = new InetSocketAddress("127.0.0.1", 8000)
      )
      .handleAll { _ => ZIO.succeed(Response.plain("Hello World\n")) }
      .serve
      .useForever
      .orDie

  def respondToWebsocketFrame(frame: Frame): Task[Frame] = ???
}
