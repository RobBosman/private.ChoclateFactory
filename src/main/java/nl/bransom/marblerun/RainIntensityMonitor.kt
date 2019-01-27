package nl.bransom.marblerun

import io.vertx.core.json.JsonObject
import io.vertx.rxjava.core.AbstractVerticle

class RainIntensityMonitor : AbstractVerticle() {

  override fun start() {
    vertx.eventBus()
        .consumer<JsonObject>(RainConstants.RAIN_INTENSITY_GET_ADDRESS)
        .toObservable()
        .withLatestFrom(vertx.eventBus()
            .consumer<JsonObject>(RainConstants.RAIN_INTENSITY_SET_ADDRESS)
            .toObservable()
            .map { it.body() }
            .map { jsonObject -> jsonObject.getDouble(RainConstants.VALUE_KEY) }
        ) { requestIntensityMessage, actualIntensity ->
          requestIntensityMessage.reply(JsonObject().put(RainConstants.VALUE_KEY, actualIntensity))
          null
        }
        .subscribe()
  }
}