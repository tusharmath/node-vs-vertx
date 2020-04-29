package vertx.scala.myapp
// import io.vertx.lang.scala._
// import io.vertx.lang.scala.ScalaVerticle.nameForVerticle
// import io.vertx.scala.core._
// import scala.concurrent.Future
// import scala.concurrent.Promise
// import scala.util.Success
// import scala.util.Failure
// vertx: io.vertx.scala.core.Vertx = io.vertx.scala.core.Vertx@1656b555
// executionContext: io.vertx.lang.scala.VertxExecutionContext = io.vertx.lang.scala.VertxExecutionContext@58c20f0d
// vertx.deployVerticle(new _root_.vertx.scala.myapp.HttpVerticle())

object Main extends App {
  // io.vertx.scala.core.Vertx
  // io.vertx.lang.scala.

  io.vertx.scala.core.Vertx.vertx().deployVerticle(new HttpVerticle())



  //  val vertx = Vertx.vertx()
  // vertx.deployVerticle(new MyVerticle());
  //vertx.deployVerticle(MyVerticle.class);


  // vertx.deployVerticle(new HttpVerticle())
}
