package example

import com.github.event.Example
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.JoinWindows
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Produced
import java.time.Duration
import java.util.*


class ExampleStream(private val service: Service) {

    fun start() {
        // Set up your Kafka Streams properties
        val streamProps = Properties()
        streamProps[StreamsConfig.APPLICATION_ID_CONFIG] = "example-stream"
        streamProps[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
        streamProps[StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG] = Serdes.String().javaClass
        streamProps[StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG] = SpecificAvroSerde::class.java
        streamProps[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"

        // Define your Kafka Streams topology
        val builder = StreamsBuilder()
        val topology = setupTopology(builder)

        // Create your Kafka Streams instance
        val streams = KafkaStreams(topology, streamProps)

        // Run your Kafka Streams application
        streams.start()
    }

    private fun setupTopology(builder: StreamsBuilder): Topology {
        // Example KStream
        val sourceString: KStream<String, String> = builder.stream<String, String>("input-topic")
        sourceString
            .mapValues(service::handle)
            .to("output-topic")

        // Avro KStream
        val avroSerde = SpecificAvroSerde<Example>(CachedSchemaRegistryClient("http://localhost:8081", 1000))
        val sourceAvro: KStream<String, Example> = builder
            .stream<String, Example>("input-topic2", Consumed.with(Serdes.String(), avroSerde))
        sourceAvro
            .mapValues(service::process)
            .to("output-topic2", Produced.with(Serdes.String(), avroSerde))

        // Join via KStream into a new KStream
        val joinedStream: KStream<String, Pair<String, Example>> = sourceString.join(
            sourceAvro,
            { sourceValue, avroValue -> Pair(sourceValue, avroValue) },
            JoinWindows.ofTimeDifferenceWithNoGrace(Duration.ofSeconds(1))
        )
        joinedStream.to("joined-topic")

        // Join via `toTable` into a new KStream
        val joinAsTable: KStream<String, Example> = sourceString
            .join(sourceAvro.toTable()) { source, avro -> Example(source, avro.getCustom()) }
        joinAsTable.to("joined-topic2", Produced.with(Serdes.String(), avroSerde))

        return builder.build()
    }
}
