package nl.cerios.reactive.chocolate

import io.vertx.core.json.JsonObject
import java.security.SecureRandom

internal class Peanut {

  private val x: Float
  private val y: Float

  init {
    val random = SecureRandom()
    x = random.nextFloat()
    y = random.nextFloat()
  }

  fun toJson(): JsonObject {
    return JsonObject()
        .put("x", x)
        .put("y", y)
  }
}