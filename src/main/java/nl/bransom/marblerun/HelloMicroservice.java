package nl.bransom.marblerun;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class HelloMicroservice extends AbstractVerticle {

  static final String ADDRESS = "hello";
  static final String MESSAGE_KEY = "message";
  static final String SERVED_BY_KEY = "served-by";
  private static final Logger LOG = LoggerFactory.getLogger(HelloMicroservice.class);

  @Override
  public void start(final Future<Void> result) {
    var consumer = vertx
        .eventBus()
        .<String>consumer(ADDRESS);
    consumer
        .toObservable()
        .subscribe(
            message -> {

              switch (new Random().nextInt(3)) {
                case 0:
                  // Just do not reply, leading to a timeout on the consumer side.
                  LOG.debug("[{}] - Not replying", message.body());
                  return;
                case 1:
                  LOG.debug("[{}] - Returning a failure", message.body());
                  // Reply with a failure
                  message.fail(500, "message processing failure");
                  return;
                default:
                  LOG.debug("[{}] - Returning success", message.body());
              }

              final var responseBody = new JsonObject().put(SERVED_BY_KEY, this.toString());
              // Check whether we have received a payload in the incoming message
              if (message.body().isEmpty()) {
                message.reply(responseBody.put(MESSAGE_KEY, "Hello"));
              } else {
                message.reply(responseBody.put(MESSAGE_KEY, "Hello " + message.body()));
              }
            },
            throwable -> LOG.error("Error processing message.", throwable));

    // This is just to update the result Future.
    consumer.completionHandler(consumerResult -> {
      if (consumerResult.succeeded()) {
        LOG.info("{} is ready to consume events on '{}'", getClass().getSimpleName(), ADDRESS);
        result.complete();
      } else {
        result.fail(consumerResult.cause());
      }
    });
  }
}
