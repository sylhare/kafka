package six

import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.eclipse.paho.client.mqttv3.MqttException
import six.model.PositionKey
import six.model.PositionValue
import java.util.*

@Throws(MqttException::class)
fun main(args: Array<String>) {
  println("*** Starting VP Producer ***")
  val settings = Properties()
  settings[ProducerConfig.CLIENT_ID_CONFIG] = "vp-producer-avro"
  settings[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = "kafka:9092"
  settings[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = KafkaAvroSerializer::class.java
  settings[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = KafkaAvroSerializer::class.java
  settings[KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG] = "http://schema-registry:8081"
  val producer: KafkaProducer<PositionKey, PositionValue> = KafkaProducer<PositionKey, PositionValue>(settings)
  Runtime.getRuntime().addShutdownHook(Thread(Runnable {
    println("### Stopping VP Producer ###")
    producer.close()
  }))
  val subscriber = AvroSubscriber(producer)
  subscriber.start()
}