package nl.cerios.reactive.chocolate

import io.vertx.core.json.JsonObject
import io.vertx.rxjava.core.AbstractVerticle
import org.slf4j.LoggerFactory
import rx.Observable
import rx.Subscriber
import java.security.SecureRandom

class PeanutProducer : AbstractVerticle() {

  private val log = LoggerFactory.getLogger(javaClass)
  private val maxIntervalMillis = 3_000.0

  override fun start() {
    vertx.eventBus()
        .consumer<JsonObject>("peanut.speed.set")
        .toObservable()
        .map<JsonObject> { it.body() }
        .map { jsonObject -> jsonObject.getDouble("value") }
        .map<Long> { intensityToIntervalMillis(it) }
        .switchMap { createPeanutObservable(it) }
        .map { it.toJson() }
        .subscribe(
            { peanutJson -> vertx.eventBus().publish("peanut.notify", peanutJson) },
            { throwable -> log.error("Error producing peanuts.", throwable) })
  }

  private fun createPeanutObservable(intervalMillis: Long): Observable<out Peanut> {
    log.debug("intervalMillis: $intervalMillis")
    return if (intervalMillis < maxIntervalMillis) {
      Observable.unsafeCreate<Peanut> { subscriber -> createDelayedPeanut(intervalMillis, subscriber) }
    } else {
      Observable.never<Peanut>()
    }
  }

  private fun createDelayedPeanut(intervalMillis: Long, subscriber: Subscriber<in Peanut>) {
    val delayMillis = sampleDelayMillis(intervalMillis)
    vertx.setTimer(delayMillis) {
      if (!subscriber.isUnsubscribed) {
        subscriber.onNext(Peanut())
        createDelayedPeanut(intervalMillis, subscriber)
      }
    }
  }

  private fun intensityToIntervalMillis(intensity: Double): Long {
    val effectiveIntensity = Math.min(Math.max(0.0, intensity), 1.0)
    log.debug("intensity: $effectiveIntensity")
    return Math.round(Math.pow(Math.E, Math.log(maxIntervalMillis) * (1.0 - effectiveIntensity)))
  }

  private fun sampleDelayMillis(intervalMillis: Long): Long {
    return Math.max(1, Math.round(2.0 * SecureRandom().nextDouble() * intervalMillis))
  }
}
