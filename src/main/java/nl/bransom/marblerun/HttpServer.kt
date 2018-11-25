package nl.bransom.marblerun

import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.ext.web.Router
import org.slf4j.LoggerFactory

const val PORT = 8080

class HttpServer : AbstractVerticle() {

  private val LOG = LoggerFactory.getLogger(javaClass)

  override fun start(result: Future<Void>) {
    val router = Router.router(vertx)

    router.get("/").handler { routingContext ->
      routingContext
          .response()
          .end("<h1>Hello from " + Thread.currentThread().name + "</h1>")
    }

    vertx
        .createHttpServer()
        .requestHandler(router::accept)
        .listen(PORT) { listenResult ->
          if (listenResult.succeeded()) {
            LOG.info("Listening on http://localhost:{}/", PORT)
            result.complete()
          } else {
            LOG.error("Error starting HTTP server", listenResult.cause())
            result.fail(listenResult.cause())
          }
        }
  }
}
