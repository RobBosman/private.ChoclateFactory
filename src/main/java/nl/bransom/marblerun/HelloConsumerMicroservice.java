package nl.bransom.marblerun;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.eventbus.Message;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Single;

import java.util.concurrent.TimeUnit;

/**
 * Send a GET request to http://localhost:8081/ to trigger this microservice.
 * It will invoke the HelloMicroservice twice and combine the responses.
 */
public class HelloConsumerMicroservice extends AbstractVerticle {

  private static final Logger LOG = LoggerFactory.getLogger(HelloConsumerMicroservice.class);
  private static final String HOST = "localhost";
  private static final int PORT = 8081;

  @Override
  public void start(final Future<Void> result) {
    final var router = Router.router(vertx);

    router.get("/").handler(this::invokeService);

    vertx
        .createHttpServer()
        .requestHandler(router::accept)
        .listen(PORT, startResult -> {
          if (startResult.succeeded()) {
            LOG.info("{} is listening on http://{}:{}/", getClass().getSimpleName(), HOST, PORT);
            result.complete();
          } else {
            result.fail(startResult.cause());
          }
        });
  }

  private void invokeService(final RoutingContext routingContext) {
    LOG.debug("Processing HTTP request");

    // Send the request messages...
    final var lukeSingle = rxSendMessage("Luke");
    final var leiaSingle = rxSendMessage("Leia");

    // ...and combine the responses
    Single.zip(
        lukeSingle,
        leiaSingle,
        (lukeResponse, leiaResponse) -> new JsonObject()
            .put("luke", lukeResponse)
            .put("leia", leiaResponse))
        .timeout(100, TimeUnit.MILLISECONDS)
        .retry(9)
        .map(JsonObject::encodePrettily)
        .subscribe(
            routingContext.response()::end,
            throwable -> routingContext.response()
                .setStatusCode(500)
                .end(throwable.getMessage()));
  }

  private Single<String> rxSendMessage(final String body) {
    return vertx
        .eventBus()
        .<JsonObject>rxSend(HelloMicroservice.ADDRESS, body)
        .map(Message::body)
        .map(json -> json.getString(HelloMicroservice.MESSAGE_KEY)
            + " from " + json.getString(HelloMicroservice.SERVED_BY_KEY)
            + " at " + json.getString(HelloMicroservice.AT_KEY));
  }
}
