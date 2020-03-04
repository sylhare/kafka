package clients

import io.confluent.monitoring.clients.interceptor.MonitoringProducerInterceptor
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerConfig.*
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.serialization.StringSerializer
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

fun main(args: Array<String>) {
  println("Starting producer.")
  // Load a driver id from an environment variable
  // if it isn't present use "driver-1"
  val driverId = System.getenv("DRIVER_ID") ?: "driver-1"
  var pos = 0

  // Configure the location of the bootstrap server, default serializers,
  // Confluent interceptors
  val settings = Properties()
  settings[CLIENT_ID_CONFIG] = driverId
  settings[BOOTSTRAP_SERVERS_CONFIG] = BOOTSTRAP_SERVER
  settings[KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
  settings[VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
  settings[INTERCEPTOR_CLASSES_CONFIG] = listOf(MonitoringProducerInterceptor::class.java)

  val producer = KafkaProducer<String, String>(settings)

  Runtime.getRuntime().addShutdownHook(Thread(Runnable {
    println("Closing producer.")
    producer.close()
  }))

  val rows = Files.readAllLines(Paths.get("$DRIVER_FILE_PREFIX$driverId.csv"), Charset.forName("UTF-8")).toTypedArray()

  // Loop forever over the driver CSV file..
  while (true) {
    val value = rows[pos]
    val record = ProducerRecord(KAFKA_TOPIC, driverId, value)
    producer.send(record) { _: RecordMetadata?, _: Exception? -> println("Sent Key:$driverId Value:$value\n") }
    Thread.sleep(1000)
    pos = (pos + 1) % rows.size
  }
}
