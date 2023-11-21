package vehicle.position.avro

import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import avro.model.PositionKey
import avro.model.PositionValue
import java.time.Duration
import java.util.*

fun main(args: Array<String>) {
  println("*** Starting VP Consumer Avro ***")
  val consumer: KafkaConsumer<PositionKey, PositionValue> = kafkaAvroConsumer()

  try {
    consumer.subscribe(listOf("vehicle-positions-avro"))
    while (true) {
      consumer.poll(Duration.ofMillis(100)).forEach {
        print("offset = ${it.offset()}, key = ${it.key()}, value = ${it.value()}\n")
      }
    }
  } finally {
    println("*** Ending VP Consumer Avro ***")
    consumer.close()
  }
}

private fun kafkaAvroConsumer(): KafkaConsumer<PositionKey, PositionValue> {
  val settings = Properties()
  settings[ConsumerConfig.GROUP_ID_CONFIG] = "vp-consumer-avro"
  settings[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = "kafka:9092"
  settings[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
  settings[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = KafkaAvroDeserializer::class.java
  settings[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = KafkaAvroDeserializer::class.java
  settings[KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG] = "true"
  settings[KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG] = "http://schema-registry:8081"

  return KafkaConsumer(settings)
}
