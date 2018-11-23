package nl.bransom.marblerun;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

  private static final Logger LOG = LoggerFactory.getLogger(Main.class);

  public static void main(final String[] args) {
    final Future<Void> whenHelloMicroserviceIsDeployed = Future.future();
    final Future<Void> whenHelloConsumerMicroserviceIsDeployed = Future.future();

    deployVertex(HelloMicroservice.class.getName(), whenHelloMicroserviceIsDeployed);
    deployVertex(HelloConsumerMicroservice.class.getName(), whenHelloConsumerMicroserviceIsDeployed);

    CompositeFuture.all(whenHelloMicroserviceIsDeployed, whenHelloConsumerMicroserviceIsDeployed)
        .setHandler(h -> {
          if (h.succeeded()) {
            LOG.info("ready to go");
          } else {
            LOG.error("Error", h.cause());
          }
        });
  }

  private static void deployVertex(final String vertexName, final Future<Void> result) {
    Vertx.vertx().deployVerticle(vertexName, deployResult -> {
      if (deployResult.succeeded()) {
        result.complete();
      } else {
        result.fail(deployResult.cause());
      }
    });
  }
}
