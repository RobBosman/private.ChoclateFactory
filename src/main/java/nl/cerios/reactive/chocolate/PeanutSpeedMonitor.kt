package nl.cerios.reactive.chocolate

import io.vertx.core.json.JsonObject
import io.vertx.rxjava.core.AbstractVerticle

class PeanutSpeedMonitor : AbstractVerticle() {

  override fun start() {
    vertx.eventBus()
        .consumer<JsonObject>("peanut.speed.get")
        .toObservable()
        .withLatestFrom(vertx.eventBus()
            .consumer<JsonObject>("peanut.speed.set")
            .toObservable()
            .map { it.body() }
            .map { jsonObject -> jsonObject.getDouble("value") }
        ) { speedRequest, actualSpeed -> speedRequest.reply(JsonObject().put("value", actualSpeed)) }
        .subscribe()
  }
}