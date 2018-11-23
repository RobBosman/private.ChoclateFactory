package nl.bransom.marblerun;

import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

  private static final Logger LOG = LoggerFactory.getLogger(Main.class);

  public static void main(final String[] args) {
    Vertx.vertx()
        .deployVerticle(HttpServer.class.getName(), deployResult -> {
          if (!deployResult.succeeded()) {
            LOG.error("Error deploying vertex " + HttpServer.class.getName(), deployResult.cause());
          }
        });
  }
}
