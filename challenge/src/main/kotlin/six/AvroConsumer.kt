package six

import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import six.model.PositionKey
import six.model.PositionValue
import java.time.Duration
import java.util.*

fun main(args: Array<String>) {
  println("*** Starting VP Consumer Avro ***")
  val settings = Properties()
  settings[ConsumerConfig.GROUP_ID_CONFIG] = "vp-consumer-avro"
  settings[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = "kafka:9092"
  settings[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
  settings[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = KafkaAvroDeserializer::class.java
  settings[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = KafkaAvroDeserializer::class.java
  settings[KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG] = "true"
  settings[KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG] = "http://schema-registry:8081"

  val consumer: KafkaConsumer<PositionKey, PositionValue> = KafkaConsumer(settings)

  try {
    consumer.subscribe(listOf("vehicle-positions-avro"))
    while (true) {
      val records: ConsumerRecords<PositionKey, PositionValue> = consumer.poll(Duration.ofMillis(100))
      records.forEach { print("offset = ${it.offset()}, key = ${it.key()}, value = ${it.value()}\n") }
    }
  } finally {
    println("*** Ending VP Consumer Avro ***")
    consumer.close()
  }
}