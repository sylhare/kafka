package json

import com.fasterxml.jackson.databind.JsonNode
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.connect.json.JsonDeserializer
import java.time.Duration
import java.util.*

object Sample {
  @JvmStatic
  fun main(args: Array<String>) {
    println("*** Starting Microservice ***")
    val consumer = consumer
    val producer = producer

    try {
      consumer.subscribe(listOf("tram-door-status-changed"))
      val cache = HashMap<String, Int?>()
      while (true) {
        val records = consumer.poll(Duration.ofMillis(100))
        records.forEach {
          print("offset = ${it.offset()}, key = ${it.key()}, value = ${it.value()}\n")
          val node = it.value()
          val operator = node["oper"].asText()
          val designation = node["desi"].asText()
          val vehicleNo = node["veh"].asText()
          val doorStatus = node["drst"].asInt()
          // assume combination of operator and vehicleNo is unique
          val key = "$operator|$vehicleNo"
          var hasChanged = true

          if (cache.containsKey(key)) hasChanged = cache[key] != doorStatus
          if (hasChanged) publishEvent(producer, operator, designation, vehicleNo, doorStatus)
          cache[key] = doorStatus
        }
      }
    } finally {
      println("*** Ending Microservice ***")
      consumer.close()
    }
  }

  private val consumer: KafkaConsumer<String, JsonNode>
    get() {
      val settings = Properties()
      settings[ConsumerConfig.GROUP_ID_CONFIG] = "tram-door-status"
      settings[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = "kafka:9092"
      settings[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
      settings[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
      settings[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = JsonDeserializer::class.java
      return KafkaConsumer(settings)
    }

  private val producer: KafkaProducer<String, DoorStatusChanged>
    get() {
      val settings = Properties()
      settings[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = "kafka:9092"
      settings[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
      settings[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
      return KafkaProducer(settings)
    }

  private fun publishEvent(producer: KafkaProducer<String, DoorStatusChanged>, operator: String, designation: String, vehicleNo: String, doorStatus: Int) {
    val type = if (doorStatus == 0) "DOOR_CLOSED" else "DOOR_OPENED"
    val event = DoorStatusChanged(operator, designation, vehicleNo, type)
    val record = ProducerRecord("tram-door-status-changed", operator, event)
    producer.send(record)
  }
}