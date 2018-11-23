package nl.bransom.marblerun;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.eventbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class HelloMicroservice extends AbstractVerticle {

  static final String ADDRESS = "hello";
  static final String MESSAGE_KEY = "message";
  static final String SERVED_BY_KEY = "served-by";
  static final String AT_KEY = "at";
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
  private static final Logger LOG = LoggerFactory.getLogger(HelloMicroservice.class);

  @Override
  public void start(final Future<Void> result) {
    final var consumer = vertx.eventBus().<String>consumer(ADDRESS);

    consumer
        .toObservable()
        .subscribe(
            this::processMessage,
            throwable -> LOG.error("Error processing message.", throwable));

    // This is just to update the result Future.
    consumer
        .rxCompletionHandler()
        .subscribe(
            x -> {
              LOG.info("{} is ready to consume messages on '{}'", getClass().getSimpleName(), ADDRESS);
              result.complete();
            },
            result::fail);
  }

  private void processMessage(final Message<String> message) {
    // inject some chaos
    switch (new Random().nextInt(3)) {
      case 0:
        // Just do not reply, leading to a timeout on the consumer side.
        LOG.debug("[{}] - Not replying", message.body());
        return;
      case 1:
        // Reply with a failure
        LOG.debug("[{}] - Returning a failure", message.body());
        message.fail(500, "message processing failure");
        return;
      default:
        LOG.debug("[{}] - Returning success ================", message.body());
    }

    final var responseBody = new JsonObject()
        .put(SERVED_BY_KEY, this.toString())
        .put(AT_KEY, LocalDateTime.now().format(DATE_FORMATTER));

    // Check whether we have received a payload in the incoming message
    if (message.body().isEmpty()) {
      message.reply(responseBody.put(MESSAGE_KEY, "Hello"));
    } else {
      message.reply(responseBody.put(MESSAGE_KEY, "Hello " + message.body()));
    }
  }
}
