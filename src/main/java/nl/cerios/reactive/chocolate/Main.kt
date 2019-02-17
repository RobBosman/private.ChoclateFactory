package nl.cerios.reactive.chocolate

import io.vertx.core.CompositeFuture
import io.vertx.core.Future
import io.vertx.rxjava.core.Vertx
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("nl.cerios.reactive.chocolate.Main")

fun main() {
  val vertx = Vertx.vertx()

  CompositeFuture
      .all(
          deployVerticle(vertx, PeanutProducer::class.java.name),
          deployVerticle(vertx, PeanutSpeedMonitor::class.java.name),
          deployVerticle(vertx, HttpEventServer::class.java.name))
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
//      .addInterceptor { context -> log.debug("EVENT '{}' = {}", context.message().address(), context.message().body()) }
//  vertx.setTimer(30000) {
//    vertx.close()
//    log.info("And... it's gone!")
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