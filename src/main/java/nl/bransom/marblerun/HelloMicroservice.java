package nl.bransom.marblerun;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloMicroservice extends AbstractVerticle {

  static final String HOST = "localhost";
  static final int PORT = 8080;
  static final String KEY = "message";
  private static final Logger LOG = LoggerFactory.getLogger(HelloMicroservice.class);

  @Override
  public void start(final Future<Void> result) {
    final Router router = Router.router(vertx);
    router.get("/").handler(this::sayHello);
    router.get("/:name").handler(this::sayHello);
    vertx.createHttpServer()
        .requestHandler(router::accept)
        .listen(PORT, startResult -> {
          if (startResult.succeeded()) {
            LOG.info("running " + getClass().getSimpleName() + " on http://" + HOST + ":" + PORT + "/");
            result.complete();
          } else {
            result.fail(startResult.cause());
          }
        });
  }

  private void sayHello(final RoutingContext rc) {
    final String nameParam = rc.pathParam("name");
    final String message = "Hello" + (nameParam == null ? "" : " " + nameParam) + " on " + Thread.currentThread().getName();
    rc.response()
        .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
        .end(new JsonObject()
            .put(KEY, message)
            .encode());
  }
}
