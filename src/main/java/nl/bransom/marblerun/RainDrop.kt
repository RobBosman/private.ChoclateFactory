package nl.bransom.marblerun

import io.vertx.core.json.JsonObject
import nl.bransom.marblerun.RainConstants.RANDOM

internal class RainDrop {

  private val x: Float = RANDOM.nextFloat()
  private val y: Float = RANDOM.nextFloat()

  fun toJson(): JsonObject {
    return JsonObject()
        .put("x", x)
        .put("y", y)
  }
}