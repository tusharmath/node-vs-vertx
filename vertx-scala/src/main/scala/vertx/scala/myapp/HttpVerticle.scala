package vertx.scala.myapp

import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.ext.web.Router

import scala.concurrent.Future

class HttpVerticle extends ScalaVerticle {
  override def startFuture(): Future[_] = {
    //Create a router to answer GET-requests to "/hello" with "world"
    val router = Router.router(vertx)
    val route = router
      .post("/")
      .handler(_.response().end("Hello World\n"))

    vertx
      .createHttpServer()
      .requestHandler(router.accept _)
      .listenFuture(3000, "0.0.0.0")
  }
}
