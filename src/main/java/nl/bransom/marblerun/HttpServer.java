package nl.bransom.marblerun;

import io.vertx.core.Future;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.http.HttpServerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServer extends AbstractVerticle {

  static final int PORT = 8080;
  private static final Logger LOG = LoggerFactory.getLogger(HttpServer.class);

  @Override
  public void start(final Future<Void> result) {
    vertx
        .createHttpServer()
        .requestHandler(this::respond)
        .listen(PORT, startResult -> {
          if (startResult.succeeded()) {
            LOG.info("running " + getClass().getSimpleName() + " on http://localhost:" + PORT + "/");
            result.complete();
          } else {
            LOG.error("Error starting HTTP server", startResult.cause());
            result.fail(startResult.cause());
          }
        });
  }

  private void respond(final HttpServerRequest request) {
    request
        .response()
        .end("<h1>Hello from " + Thread.currentThread().getName() + "</h1>");
  }
}
