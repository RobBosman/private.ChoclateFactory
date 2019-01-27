package nl.bransom.microservice

import com.mongodb.client.model.Projections
import com.mongodb.reactivestreams.client.MongoClients
import com.mongodb.reactivestreams.client.Success
import nl.bransom.microservice.SubscriberHelpers.PrintDocumentSubscriber
import nl.bransom.microservice.SubscriberHelpers.PrintSubscriber
import org.bson.Document
import org.junit.jupiter.api.Test
import rx.Observable
import rx.RxReactiveStreams.toObservable
import java.util.concurrent.TimeUnit.SECONDS
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObservableTest {

  @Test
  fun testBlocking() {
    val mongoClient = MongoClients.create()

    val tracks = mongoClient
        .getDatabase("microservice")
        .getCollection("hello")

    val insertSubscriber = PrintSubscriber<Success>("OK")
    tracks
        .insertOne(Document("name", "MongoDB")
            .append("type", "database")
            .append("count", 1)
            .append("info", Document("x", 203).append("y", 102)))
        .subscribe(insertSubscriber)
    insertSubscriber.await() // Block for the publisher to complete

    val printSubscriber = PrintDocumentSubscriber()
    tracks
        .find()
        .projection(Projections.excludeId())
        .subscribe(printSubscriber)
    printSubscriber.await() // Block for the publisher to complete

    mongoClient.close()
  }

  @Test
  fun testInteropWithRxJava() {
    val database = Fixture.defaultDatabase

    toObservable(database?.drop())
        .timeout(10, SECONDS)
        .toBlocking()
        .single()

    val observers = ArrayList<Observable<Success>>()
    val uppercaseNames = ArrayList<String>()

    for (i in 0..24) {
      val name = "collectionNumber $i"
      observers.add(toObservable(database?.createCollection(name)))
      uppercaseNames.add(name.toUpperCase())
    }

    assertEquals(25, Observable
        .merge(observers)
        .timeout(10, SECONDS)
        .toList()
        .toBlocking()
        .single()
        .size)

    val collectionNames = toObservable(database?.listCollectionNames())
        .filter { s -> s.startsWith("c") }
        .map { s -> s.toUpperCase() }
        .toList()
        .timeout(10, SECONDS)
        .toBlocking()
        .single()

    assertEquals(uppercaseNames.size, collectionNames.size)
    assertTrue { collectionNames.containsAll(uppercaseNames) }
    assertTrue { uppercaseNames.containsAll(collectionNames) }

    toObservable(database?.drop())
        .timeout(10, SECONDS)
        .toBlocking()
        .single()
  }
}