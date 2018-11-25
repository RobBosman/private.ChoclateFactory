package nl.bransom.marblerun

import io.vertx.core.Future
import io.vertx.core.json.JsonObject
import io.vertx.rxjava.core.AbstractVerticle
import io.vertx.rxjava.core.eventbus.Message
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

const val ADDRESS = "hello"
const val MESSAGE_KEY = "message"
const val SERVED_BY_KEY = "served-by"
const val AT_KEY = "at"

class HelloMicroservice : AbstractVerticle() {

  private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
  private val LOG = LoggerFactory.getLogger(javaClass)

  override fun start(result: Future<Void>) {
    val consumer = vertx.eventBus().consumer<String>(ADDRESS)

    consumer
        .toObservable()
        .subscribe(
            { message -> processMessageInChaos(message) },
            { throwable -> LOG.error("Error processing message.", throwable) })

    // This is just to update the result Future.
    consumer
        .rxCompletionHandler()
        .subscribe(
            {
              LOG.info("Ready to consume messages on '{}'", ADDRESS)
              result.complete()
            },
            { throwable -> result.fail(throwable) })
  }

  private fun processMessageInChaos(message: Message<String>) {
    // inject some chaos
    when (Random().nextInt(3)) {
      0 -> {
        // Just do not reply, leading to a timeout on the consumer side.
        LOG.debug("[{}] - Not replying", message.body())
      }
      1 -> {
        // Reply with a failure
        LOG.debug("[{}] - Returning a failure", message.body())
        message.fail(500, "message processing failure")
      }
      else -> {
        LOG.debug("[{}] - Returning success ================", message.body())
        processMessage(message)
      }
    }
  }

  private fun processMessage(message: Message<String>) {
    val responseBody = JsonObject()
        .put(SERVED_BY_KEY, this.toString())
        .put(AT_KEY, LocalDateTime.now().format(DATE_FORMATTER))

    // Check whether we have received a payload in the incoming message
    if (message.body().isEmpty()) {
      message.reply(responseBody.put(MESSAGE_KEY, "Hello"))
    } else {
      message.reply(responseBody.put(MESSAGE_KEY, "Hello " + message.body()))
    }
  }
}
