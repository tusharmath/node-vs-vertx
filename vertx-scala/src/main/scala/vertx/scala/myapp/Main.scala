package vertx.scala.myapp

import io.vertx.core.VertxOptions
import io.vertx.scala.core.DeploymentOptions

object Main extends App {
  io.vertx.scala.core.Vertx
    .vertx()
    .deployVerticle(
      name = s"scala:${classOf[HttpVerticle].getName}",
      options = DeploymentOptions().setInstances(VertxOptions.DEFAULT_EVENT_LOOP_POOL_SIZE)
    )

}
