package subscriber

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import java.util.*

fun main(args: Array<String>) {
  println("*** Starting VP Producer ***")

  val settings = Properties()
  settings[ProducerConfig.CLIENT_ID_CONFIG] = "vp-producer"
  settings[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = "kafka:9092"
  settings[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
  settings[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java

  val producer = KafkaProducer<String, String>(settings)

  Runtime.getRuntime().addShutdownHook(Thread(Runnable {
    println("### Stopping VP Producer ###")
    producer.close()
  }))

  val subscriber = Subscriber(producer)
  subscriber.start()
}