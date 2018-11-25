package nl.bransom.marblerun

import com.mongodb.MongoTimeoutException
import org.bson.Document
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import java.lang.String.format
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Subscriber helper implementations for the Quick Tour.
 */
object SubscriberHelpers {

  /**
   * A Subscriber that stores the publishers results and provides a latch so can block on completion.
   *
   * @param <T> The publishers result type
  </T> */
  open class ObservableSubscriber<T> internal constructor() : Subscriber<T> {
    private val received: MutableList<T>
    private val errors: MutableList<Throwable>
    private val latch: CountDownLatch
    @Volatile
    var subscription: Subscription? = null
      private set
    @Volatile
    var isCompleted: Boolean = false
      private set

    val error: Throwable?
      get() = if (errors.size > 0) errors[0] else null

    init {
      this.received = ArrayList()
      this.errors = ArrayList()
      this.latch = CountDownLatch(1)
    }

    override fun onSubscribe(s: Subscription) {
      subscription = s
    }

    override fun onNext(t: T) {
      received.add(t)
    }

    override fun onError(t: Throwable) {
      errors.add(t)
      onComplete()
    }

    override fun onComplete() {
      isCompleted = true
      latch.countDown()
    }

    fun getReceived(): List<T> {
      return received
    }

    @Throws(Throwable::class)
    operator fun get(timeout: Long, unit: TimeUnit): List<T> {
      return await(timeout, unit).getReceived()
    }

    @Throws(Throwable::class)
    @JvmOverloads
    fun await(timeout: Long = java.lang.Long.MAX_VALUE, unit: TimeUnit = TimeUnit.MILLISECONDS): ObservableSubscriber<T> {
      subscription!!.request(Integer.MAX_VALUE.toLong())
      if (!latch.await(timeout, unit)) {
        throw MongoTimeoutException("Publisher onComplete timed out")
      }
      if (!errors.isEmpty()) {
        throw errors[0]
      }
      return this
    }
  }

  /**
   * A Subscriber that immediately requests Integer.MAX_VALUE onSubscribe
   *
   * @param <T> The publishers result type
  </T> */
  open class OperationSubscriber<T> : ObservableSubscriber<T>() {

    override fun onSubscribe(s: Subscription) {
      super.onSubscribe(s)
      s.request(Integer.MAX_VALUE.toLong())
    }
  }

  /**
   * A Subscriber that prints a message including the received items on completion
   *
   * @param <T> The publishers result type
  </T> */
  class PrintSubscriber<T>(private val message: String) : OperationSubscriber<T>() {

    override fun onComplete() {
      println(format(message, getReceived()))
      super.onComplete()
    }
  }

  /**
   * A Subscriber that prints the json version of each document
   */
  class PrintDocumentSubscriber : OperationSubscriber<Document>() {

    override fun onNext(t: Document) {
      super.onNext(t)
      println(t.toJson())
    }
  }
}