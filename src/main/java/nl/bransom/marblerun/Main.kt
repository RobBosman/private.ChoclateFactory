package nl.bransom.marblerun

import io.vertx.core.CompositeFuture
import io.vertx.core.Future
import io.vertx.rxjava.core.Vertx
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("nl.bransom.marblerun.Main")

fun main() {
  val vertx = Vertx.vertx()

  CompositeFuture
      .all(
          deployVerticle(vertx, RainMaker::class.java.name),
          deployVerticle(vertx, RainIntensityMonitor::class.java.name),
          deployVerticle(vertx, RainServer::class.java.name))
      .setHandler { result ->
        if (result.succeeded()) {
          log.info("We have hyperdrive, captain.")
        } else {
          log.error("Error", result.cause())
        }
      }

// NOTE - do not log (and swallow!) messages that are 'sent'; only log messages that are 'published'!
//  vertx
//      .eventBus()
//      .addInterceptor { context -> LOG.debug("EVENT '{}' = {}", context.message().address(), context.message().body()) }
//  vertx.setTimer(30000) {
//    vertx.close()
//    LOG.info("And... it's gone!")
//    System.exit(0)
//  }
}

private fun deployVerticle(vertx: Vertx, verticleName: String): Future<Void> {
  val result = Future.future<Void>()
  vertx.deployVerticle(verticleName) { deployResult ->
    if (deployResult.succeeded()) {
      result.complete()
    } else {
      result.fail(deployResult.cause())
    }
  }
  return result
}