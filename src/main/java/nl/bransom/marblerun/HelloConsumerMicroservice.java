package nl.bransom.marblerun;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.eventbus.Message;
import io.vertx.rxjava.core.http.HttpServerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Single;

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
    vertx.createHttpServer()
        .requestHandler(this::invokeService)
        .listen(PORT, startResult -> {
          if (startResult.succeeded()) {
            LOG.info("{} is listening on http://{}:{}/", getClass().getSimpleName(), HOST, PORT);
            result.complete();
          } else {
            result.fail(startResult.cause());
          }
        });
  }

  private void invokeService(final HttpServerRequest httpRequest) {
    Single.zip(
        rxSendMessage("Luke"),
        rxSendMessage("Leia"),
        (lukeResponse, leiaResponse) -> { // combine both requests
          return new JsonObject()
              .put("luke", lukeResponse)
              .put("leia", leiaResponse);
        })
        .map(JsonObject::encodePrettily)
        .subscribe(
            httpRequest.response()::end,
            t -> httpRequest.response()
                .setStatusCode(500)
                .end(t.getMessage()));
  }

  private Single<String> rxSendMessage(final String body) {
    return vertx.eventBus()
        .<JsonObject>rxSend(HelloMicroservice.ADDRESS, body)
        .map(Message::body)
        .map(json -> json.getString(HelloMicroservice.MESSAGE_KEY) + " from " + json.getString(HelloMicroservice.SERVED_BY_KEY));
  }
}
