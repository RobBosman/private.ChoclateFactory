package nl.bransom.marblerun;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.ext.web.client.HttpResponse;
import io.vertx.rxjava.ext.web.client.WebClient;
import io.vertx.rxjava.ext.web.codec.BodyCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Single;

/**
 * Send a GET request to http://localhost:8081/ to trigger this microservice. It will invoke the HelloMicroservice twice and combine the responses.
 */
public class HelloConsumerMicroservice extends AbstractVerticle {

  private static final Logger LOG = LoggerFactory.getLogger(HelloConsumerMicroservice.class);
  private static final String HOST = "localhost";
  private static final int PORT = 8081;

  private WebClient client;

  @Override
  public void start(final Future<Void> result) {
    client = WebClient.create(vertx);

    final Router router = Router.router(vertx);
    router.get("/").handler(this::invokeMyFirstMicroservice);

    vertx.createHttpServer()
        .requestHandler(router::accept)
        .listen(PORT, startResult -> {
          if (startResult.succeeded()) {
            LOG.info("running " + getClass().getSimpleName() + " on http://" + HOST + ":" + PORT + "/");
            result.complete();
          } else {
            result.fail(startResult.cause());
          }
        });
  }

  private void invokeMyFirstMicroservice(final RoutingContext routingContext) {
    final Single<HttpResponse<JsonObject>> lukeSingle = client
        .get(HelloMicroservice.PORT, HelloMicroservice.HOST, "/Luke")
        .as(BodyCodec.jsonObject())
        .rxSend();
    final Single<HttpResponse<JsonObject>> leiaSingle = client
        .get(HelloMicroservice.PORT, HelloMicroservice.HOST, "/Leia")
        .as(BodyCodec.jsonObject())
        .rxSend();

    Single.zip(lukeSingle, leiaSingle,
        (lukeResponse, leiaResponse) -> {
          // We have the result of both requests
          return new JsonObject()
              .put("luke", lukeResponse.body().getString(HelloMicroservice.KEY))
              .put("leia", leiaResponse.body().getString(HelloMicroservice.KEY));
        })
        .map(JsonObject::encodePrettily)
        .subscribe(
            jsonResult -> routingContext.response().end(jsonResult),
            t -> routingContext.response().end(new JsonObject().encodePrettily()));
  }
}
