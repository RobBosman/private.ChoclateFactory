package nl.bransom.marblerun

import java.security.SecureRandom

object RainConstants {

  const val SERVER_PORT = 8080

  val RANDOM = SecureRandom()

  const val MAX_INTERVAL_MILLIS = 3_000.0

  const val RAIN_INTENSITY_SET_ADDRESS = "rain.intensity.set"
  const val RAIN_INTENSITY_GET_ADDRESS = "rain.intensity.get"
  const val RAIN_DROP_NOTIFY_ADDRESS = "rain.drop.notify"

  const val VALUE_KEY = "value"
}