package nl.bransom.marblerun

import io.vertx.core.json.JsonObject
import io.vertx.rxjava.core.AbstractVerticle
import org.slf4j.LoggerFactory
import rx.Observable
import rx.Subscriber

class RainMaker : AbstractVerticle() {

  private val log = LoggerFactory.getLogger(javaClass)

  override fun start() {
    vertx.eventBus()
        .consumer<JsonObject>(RainConstants.RAIN_INTENSITY_SET_ADDRESS)
        .toObservable()
        .map<JsonObject> { it.body() }
        .map { jsonObject -> jsonObject.getDouble(RainConstants.VALUE_KEY) }
        .map<Long> { intensityToIntervalMillis(it) }
        .switchMap { this.createRainDropObservable(it) }
        .map { it.toJson() }
        .subscribe(
            { rainDropJson -> vertx.eventBus().publish(RainConstants.RAIN_DROP_NOTIFY_ADDRESS, rainDropJson) },
            { throwable -> log.error("Error making rain.", throwable) })
  }

  private fun createRainDropObservable(intervalMillis: Long): Observable<out RainDrop> {
    log.debug("intervalMillis: $intervalMillis")
    return if (intervalMillis < RainConstants.MAX_INTERVAL_MILLIS) {
      Observable.unsafeCreate<RainDrop> { subscriber -> createDelayedRainDrop(intervalMillis, subscriber) }
    } else {
      Observable.never<RainDrop>()
    }
  }

  private fun createDelayedRainDrop(intervalMillis: Long, subscriber: Subscriber<in RainDrop>) {
    val delayMillis = sampleDelayMillis(intervalMillis)
    vertx.setTimer(delayMillis) {
      if (!subscriber.isUnsubscribed) {
        subscriber.onNext(RainDrop())
        createDelayedRainDrop(intervalMillis, subscriber)
      }
    }
  }

  private fun intensityToIntervalMillis(intensity: Double): Long {
    val effectiveIntensity = Math.min(Math.max(0.0, intensity), 1.0)
    log.debug("intensity: {}", effectiveIntensity)
    return Math.round(Math.pow(Math.E, Math.log(RainConstants.MAX_INTERVAL_MILLIS) * (1.0 - effectiveIntensity)))
  }

  private fun sampleDelayMillis(intervalMillis: Long): Long {
    return Math.max(1, Math.round(2.0 * RainConstants.RANDOM.nextDouble() * intervalMillis))
  }
}
