package nl.bransom.marblerun;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloMicroservice extends AbstractVerticle {

  static final String ADDRESS = "hello";
  static final String MESSAGE_KEY = "message";
  static final String SERVED_BY_KEY = "served-by";
  private static final Logger LOG = LoggerFactory.getLogger(HelloMicroservice.class);

  @Override
  public void start(final Future<Void> result) {
    vertx
        .eventBus()
        .<String>consumer(ADDRESS,
            message -> {
              final JsonObject responseBody = new JsonObject()
                  .put(SERVED_BY_KEY, this.toString());
              // Check whether we have received a payload in the incoming message
              if (message.body().isEmpty()) {
                message.reply(responseBody.put(MESSAGE_KEY, "Hello"));
              } else {
                message.reply(responseBody.put(MESSAGE_KEY, "Hello " + message.body()));
              }
            })
        .completionHandler(consumerResult -> {
          if (consumerResult.succeeded()) {
            LOG.info("{} is ready to consume events on '{}'", getClass().getSimpleName(), ADDRESS);
            result.complete();
          } else {
            result.fail(consumerResult.cause());
          }
        });
  }
}
