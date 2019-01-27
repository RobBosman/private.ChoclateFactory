package nl.bransom.microservice

import com.mongodb.ConnectionString
import com.mongodb.MongoCommandException
import com.mongodb.MongoNamespace
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import com.mongodb.reactivestreams.client.MongoCollection
import com.mongodb.reactivestreams.client.MongoDatabase
import org.bson.Document
import rx.RxReactiveStreams
import java.util.concurrent.TimeUnit.SECONDS

object Fixture {

  const val DEFAULT_URI = "mongodb://localhost:27017"
  const val MONGODB_URI_SYSTEM_PROPERTY_NAME = "org.mongodb.test.uri"
  const val defaultDatabaseName = "JavaDriverReactiveTest"

  private var connectionString: ConnectionString? = null
  private var mongoClient: MongoClient? = null

  val defaultDatabase: MongoDatabase?
    get() = getMongoClient()?.getDatabase(defaultDatabaseName)

  @Synchronized
  fun getMongoClient(): MongoClient? {
    if (mongoClient == null) {
      mongoClient = MongoClients.create(getConnectionString())
      Runtime.getRuntime().addShutdownHook(ShutdownHook())
    }
    return mongoClient
  }

  @Synchronized
  fun getConnectionString(): ConnectionString? {
    if (connectionString == null) {
      val mongoURIProperty = System.getProperty(MONGODB_URI_SYSTEM_PROPERTY_NAME)
      val mongoURIString = if (mongoURIProperty == null || mongoURIProperty.isEmpty())
        DEFAULT_URI
      else
        mongoURIProperty
      connectionString = ConnectionString(mongoURIString)
    }
    return connectionString
  }

  @Throws(Throwable::class)
  fun initializeCollection(namespace: MongoNamespace): MongoCollection<Document>? {
    val database = getMongoClient()?.getDatabase(namespace.databaseName)
    try {
      RxReactiveStreams
          .toObservable(database
              ?.runCommand(Document("drop", namespace.collectionName)))
          .timeout(10, SECONDS).toBlocking().toIterable()
    } catch (e: MongoCommandException) {
      if (!e.errorMessage.startsWith("ns not found")) {
        throw e
      }
    }
    return database?.getCollection(namespace.collectionName)
  }

  @Throws(Throwable::class)
  fun dropDatabase(name: String?) {
    if (name == null) {
      return
    }
    try {
      RxReactiveStreams
          .toObservable(getMongoClient()
              ?.getDatabase(name)
              ?.runCommand(Document("dropDatabase", 1)))
          .timeout(10, SECONDS).toBlocking().toIterable()
    } catch (e: MongoCommandException) {
      if (!e.errorMessage.startsWith("ns not found")) {
        throw e
      }
    }
  }

  @Throws(Throwable::class)
  fun drop(namespace: MongoNamespace) {
    try {
      RxReactiveStreams
          .toObservable(getMongoClient()
              ?.getDatabase(namespace.databaseName)
              ?.runCommand(Document("drop", namespace.collectionName)))
          .timeout(10, SECONDS)
          .toBlocking()
          .toIterable()
    } catch (e: MongoCommandException) {
      if (!e.errorMessage.contains("ns not found")) {
        throw e
      }
    }
  }

  internal class ShutdownHook : Thread() {
    override fun run() {
      try {
        dropDatabase(defaultDatabaseName)
      } catch (e: Throwable) {
        // ignore
      }
      mongoClient!!.close()
      mongoClient = null
    }
  }
}