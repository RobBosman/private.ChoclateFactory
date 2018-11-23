package nl.bransom.marblerun;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

  private static final Logger LOG = LoggerFactory.getLogger(Main.class);

  public static void main(final String[] args) {
    final Vertx vertx = Vertx.vertx();

    CompositeFuture
        .all(
            deployVerticle(vertx, HelloMicroservice.class.getName()),
//            deployVerticle(vertx, HelloMicroservice.class.getName()),
//            deployVerticle(vertx, HelloMicroservice.class.getName()),
            deployVerticle(vertx, HelloConsumerMicroservice.class.getName()))
        .setHandler(result -> {
          if (result.succeeded()) {
            LOG.info("We have hyperdrive, captain.");
          } else {
            LOG.error("Error", result.cause());
          }
        });

    // Log published messages only!
//    vertx.eventBus()
//        .addInterceptor(context -> LOG.debug("EVENT '{}' = {}", context.message().address(), context.message().body()));
    vertx.setTimer(30000, timerId -> {
      vertx.close();
      LOG.info("And... it's gone!");
      System.exit(0);
    });
  }

  private static Future<Void> deployVerticle(final Vertx vertx, final String verticleName) {
    final Future<Void> result = Future.future();
    vertx.deployVerticle(verticleName,
        deployResult -> {
          if (deployResult.succeeded()) {
            result.complete();
          } else {
            result.fail(deployResult.cause());
          }
        });
    return result;
  }
}
