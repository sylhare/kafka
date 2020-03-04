package clients

import io.confluent.monitoring.clients.interceptor.MonitoringConsumerInterceptor
import org.apache.kafka.clients.consumer.ConsumerConfig.*
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import java.time.Duration
import java.util.*

fun main(args: Array<String>) {
  println("Starting Consumer.")

  // Confluent interceptors
  // Configure the group id, location of the bootstrap server, default deserializers,
  val settings = Properties()
  settings[GROUP_ID_CONFIG] = "Consumer"
  settings[BOOTSTRAP_SERVERS_CONFIG] = BOOTSTRAP_SERVER
  settings[AUTO_OFFSET_RESET_CONFIG] = "earliest"
  settings[KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
  settings[VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
  settings[INTERCEPTOR_CLASSES_CONFIG] = listOf(MonitoringConsumerInterceptor::class.java)
  val consumer = KafkaConsumer<String, String>(settings)

  try { // Subscribe to our topic
    consumer.subscribe(listOf(KAFKA_TOPIC))
    while (true) {
      val records = consumer.poll(Duration.ofMillis(100))
      records.forEach {
        print("Key:${it.key()} Value:${it.value()} [partition ${it.partition()}]\n")
      }
    }
  } finally { // Clean up when the application exits or errors
    println("Closing consumer.")
    consumer.close()
  }
}
