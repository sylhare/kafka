package vehicle.offset

import org.apache.kafka.clients.consumer.ConsumerConfig.*
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Duration
import java.util.*

const val OFFSET_FILE_PREFIX = "./src/main/resources/offsets/offset_"

fun main(args: Array<String>) {
  println("*** Starting Prev Data Consumer ***")
  val consumer = kafkaPrevConsumer()
  //val listener = ConsumerListener(consumer)
  val listener = createListener(consumer)

  try {
    consumer.subscribe(listOf("vehicle-positions"), listener)
    while (true) {
      val records = consumer.poll(Duration.ofMillis(100))
      records.forEach {
        print("offset = ${it.offset()}, key = ${it.key()}, value = ${it.value()}\n")
        // To save manually the offset
        Files.write(
            Paths.get(OFFSET_FILE_PREFIX + it.partition()),
            (it.offset() + 1).toString().toByteArray())
      }
    }
  } finally {
    println("*** Ending VP Consumer ***")
    consumer.close()
  }
}

private fun kafkaPrevConsumer(): KafkaConsumer<String, String> {
  val settings = Properties()
  settings[GROUP_ID_CONFIG] = "prev-data-consumer"
  settings[BOOTSTRAP_SERVERS_CONFIG] = "kafka:9092"
  settings[AUTO_OFFSET_RESET_CONFIG] = "earliest"
  settings[KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
  settings[VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java

  return KafkaConsumer(settings)
}
