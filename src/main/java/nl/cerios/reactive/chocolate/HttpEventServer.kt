package nl.cerios.reactive.chocolate

import io.vertx.core.Future
import io.vertx.ext.bridge.PermittedOptions
import io.vertx.ext.web.handler.sockjs.BridgeOptions
import io.vertx.rxjava.core.AbstractVerticle
import io.vertx.rxjava.ext.web.Router
import io.vertx.rxjava.ext.web.handler.StaticHandler
import io.vertx.rxjava.ext.web.handler.sockjs.SockJSHandler
import org.slf4j.LoggerFactory

class HttpEventServer : AbstractVerticle() {

  private val log = LoggerFactory.getLogger(javaClass)
  private val serverPort = 8080

  override fun start(futureResult: Future<Void>) {

    val router = Router.router(vertx)

    router.route("/eventbus/*")
        .handler(SockJSHandler.create(vertx)
            .bridge(BridgeOptions()
                .addInboundPermitted(PermittedOptions().setAddress("peanut.speed.get"))
                .addInboundPermitted(PermittedOptions().setAddress("peanut.speed.set"))
                .addOutboundPermitted(PermittedOptions().setAddress("peanut.notify"))
                .addOutboundPermitted(PermittedOptions().setAddress("peanut.speed.set"))))

    router.route()
        .handler(StaticHandler.create("www").setIndexPage("chocolateFactory.html"))

    vertx.createHttpServer()
        .requestHandler(router)
        .listen(serverPort) { result ->
          if (result.succeeded()) {
            log.info("Server is listening on http://localhost:$serverPort/")
            futureResult.complete()
          } else {
            futureResult.fail(result.cause())
          }
        }
  }
}