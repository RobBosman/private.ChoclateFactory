package nl.bransom.marblerun;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestSuite;
import org.junit.jupiter.api.Test;

class HttpServerTest {

  @Test
  void test() {
    final var vertx = Vertx.vertx();
    TestSuite
        .create("TestSuite")
        .before(testContext ->
            vertx
                .exceptionHandler(testContext.exceptionHandler())
                .deployVerticle(HttpServer.class.getName(), testContext.asyncAssertSuccess()))
        .test("pingTest", testContext -> {
          final var async = testContext.async();
          vertx
              .exceptionHandler(testContext.exceptionHandler())
              .createHttpClient()
              .getNow(HttpServer.PORT, "localhost", "/",
                  response -> response.handler(body -> {
                    testContext.assertTrue(body.toString().contains("Hello from "));
                    async.complete();
                  }));
        })
        .after(testContext -> vertx.close(testContext.asyncAssertSuccess()))
        .run()
        .awaitSuccess();
  }
}
