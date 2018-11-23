package nl.bransom.marblerun;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServer extends AbstractVerticle {

  static final int PORT = 8080;
  private static final Logger LOG = LoggerFactory.getLogger(HttpServer.class);

  @Override
  public void start(final Future<Void> result) {
    final var router = Router.router(vertx);

    router.get("/").handler(routingContext ->
        routingContext
            .response()
            .end("<h1>Hello from " + Thread.currentThread().getName() + "</h1>"));

    vertx
        .createHttpServer()
        .requestHandler(router::accept)
        .listen(PORT, listenResult -> {
          if (listenResult.succeeded()) {
            LOG.info("{} is listening on http://localhost:{}/", getClass().getSimpleName(), PORT);
            result.complete();
          } else {
            LOG.error("Error starting HTTP server", listenResult.cause());
            result.fail(listenResult.cause());
          }
        });
  }
}
