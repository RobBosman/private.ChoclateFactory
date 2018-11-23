package nl.bransom.marblerun;

import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

  private static final Logger LOG = LoggerFactory.getLogger(Main.class);

  public static void main(final String[] args) {
    final Vertx vertx = Vertx.vertx();

    final Future<Void> whenHelloMicroserviceIsDeployed = Future.future();
    final Future<Void> whenHelloConsumerMicroserviceIsDeployed = Future.future();

    vertx.deployVerticle(HelloMicroservice.class.getName(), toAsyncResultHandler(whenHelloMicroserviceIsDeployed));
    vertx.deployVerticle(HelloConsumerMicroservice.class.getName(), toAsyncResultHandler(whenHelloConsumerMicroserviceIsDeployed));

    CompositeFuture
        .all(whenHelloMicroserviceIsDeployed, whenHelloConsumerMicroserviceIsDeployed)
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

  private static Handler<AsyncResult<String>> toAsyncResultHandler(final Future<Void> result) {
    return deployResult -> {
      if (deployResult.succeeded()) {
        result.complete();
      } else {
        result.fail(deployResult.cause());
      }
    };
  }
}
