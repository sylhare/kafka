package vehicle.position.avro

import avro.model.PositionKey
import avro.model.PositionValue
import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig
import io.confluent.kafka.serializers.subject.TopicRecordNameStrategy
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import java.util.*

fun main(args: Array<String>) {
    println("*** Starting VP Producer ***")
    val producer: KafkaProducer<PositionKey, PositionValue> = kafkaAvroProducer()
    Runtime.getRuntime().addShutdownHook(Thread {
        println("### Stopping VP Producer ###")
        producer.close()
    })
    val subscriber = AvroSubscriber(producer)
    subscriber.start()
}

private fun kafkaAvroProducer(): KafkaProducer<PositionKey, PositionValue> {
    val settings = Properties()
    settings[ProducerConfig.CLIENT_ID_CONFIG] = "vp-producer-avro"
    settings[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = "kafka:9092"
    settings[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = KafkaAvroSerializer::class.java
    settings[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = KafkaAvroSerializer::class.java
    settings[KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG] = "http://schema-registry:8081"
    settings[KafkaAvroSerializerConfig.VALUE_SUBJECT_NAME_STRATEGY] = TopicRecordNameStrategy::class.java
    return KafkaProducer(settings)
}
