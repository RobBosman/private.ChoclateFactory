package nl.bransom.marblerun

import io.vertx.core.Future
import io.vertx.ext.bridge.PermittedOptions
import io.vertx.ext.web.handler.sockjs.BridgeOptions
import io.vertx.rxjava.core.AbstractVerticle
import io.vertx.rxjava.ext.web.Router
import io.vertx.rxjava.ext.web.handler.StaticHandler
import io.vertx.rxjava.ext.web.handler.sockjs.SockJSHandler
import org.slf4j.LoggerFactory

class RainServer : AbstractVerticle() {

  private val log = LoggerFactory.getLogger(javaClass)

  override fun start(futureResult: Future<Void>) {

    val router = Router.router(vertx)

    router.route("/eventbus/*")
        .handler(SockJSHandler.create(vertx)
            .bridge(BridgeOptions()
                .addInboundPermitted(PermittedOptions().setAddress(RainConstants.RAIN_INTENSITY_GET_ADDRESS))
                .addInboundPermitted(PermittedOptions().setAddress(RainConstants.RAIN_INTENSITY_SET_ADDRESS))
                .addOutboundPermitted(PermittedOptions().setAddress(RainConstants.RAIN_DROP_NOTIFY_ADDRESS))
                .addOutboundPermitted(PermittedOptions().setAddress(RainConstants.RAIN_INTENSITY_SET_ADDRESS))))

    router.route()
        .handler(StaticHandler.create("www").setIndexPage("rain.html"))

    vertx.createHttpServer()
        .requestHandler { router.accept(it) }
        .listen(RainConstants.SERVER_PORT) { result ->
          if (result.succeeded()) {
            log.info("Server is now listening on http://localhost:${RainConstants.SERVER_PORT}/")
            futureResult.complete()
          } else {
            futureResult.fail(result.cause())
          }
        }
  }
}