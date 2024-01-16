package vehicle.position.avro

import avro.model.PositionKey
import avro.model.PositionValue
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.KStream
import java.util.*


fun main(args: Array<String>) {
    println("*** Starting VP Stream Avro Consumer ***")
    try {
        val streamBuilder = StreamsBuilder()
        val configuration = mapOf(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG to "http://schema-registry:8081")
        val kerSerde = SpecificAvroSerde<PositionKey>().also { it.configure(configuration, true) }
        val valueSerde = GenericAvroSerde().also { it.configure(configuration, false) }
        val positionConsumerStream: KStream<PositionKey, GenericRecord> = streamBuilder
            .stream("vehicle-positions-avro", Consumed.with(kerSerde, valueSerde))

        positionConsumerStream.foreach { key, value ->
            println("key = $key, value = $value, isPositionValue=${value is PositionValue}")
        }

        val kafkaStreams = buildKafkaStreams(streamBuilder)
        kafkaStreams.start()
    } catch (e: Exception) {
        println("Error occurred while running Kafka Stream. $e")
    } finally {
        println("*** Ending VP Stream Avro Consumer ***")
    }
}

private fun buildKafkaStreams(streamBuilder: StreamsBuilder): KafkaStreams {
    val settings = Properties()
    settings[StreamsConfig.APPLICATION_ID_CONFIG] = "vp-avro-stream"
    settings[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "kafka:9092"
    //settings[KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG] = "http://schema-registry:8081"
    val kafkaStreams = KafkaStreams(streamBuilder.build(), settings)
    kafkaStreams.setStateListener { newState: KafkaStreams.State, _: KafkaStreams.State? ->
        if (KafkaStreams.State.ERROR == newState) {
            println("Kafka stream in error state, shutdown stream")
            kafkaStreams.close()
        }
    }
    return kafkaStreams
}
