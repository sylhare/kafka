package four

import java.time.Duration
import java.util.Arrays
import java.util.Properties
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer


fun main(args: Array<String>) {
  println("*** Starting VP Consumer ***")
  val settings = Properties()
  settings[ConsumerConfig.GROUP_ID_CONFIG] = "vp-consumer"
  settings[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = "kafka:9092"
  settings[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
  settings[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
  settings[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
  val consumer: KafkaConsumer<String, String> = KafkaConsumer(settings)
  try {
    consumer.subscribe(Arrays.asList("vehicle-positions"))
    while (true) {
      val records: ConsumerRecords<String, String> = consumer.poll(Duration.ofMillis(100))
      for (record in records) System.out.printf("offset = %d, key = %s, value = %s\n", record.offset(), record.key(), record.value())
    }
  } finally {
    println("*** Ending VP Consumer ***")
    consumer.close()
  }
}