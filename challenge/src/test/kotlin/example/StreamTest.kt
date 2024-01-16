package example

import io.confluent.kafka.schemaregistry.avro.AvroSchema
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import io.confluent.kafka.schemaregistry.testutil.MockSchemaRegistry
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde
import org.apache.avro.Schema
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.*
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Produced
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class StreamTest {
    private val registryScope: String = "StreamTest"
    private val mockRegistryUrl = "mock://$registryScope"
    private val genericAvroSerde = GenericAvroSerde().apply {
        configure(mapOf(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG to mockRegistryUrl), false)
    }
    private val inputTopic = "inputTopic"
    private val outputTopic = "outputTopic"

    private val streamsConfiguration = Properties().also {
        it[StreamsConfig.APPLICATION_ID_CONFIG] = "stream-example"
        it[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "mock://bootstrap-server"
        it[StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG] = Serdes.String().javaClass.getName()
        it[StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG] = GenericAvroSerde::class.java
        it[KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG] = mockRegistryUrl
    }

    private lateinit var mockSchemaRegistryClient: SchemaRegistryClient
    private lateinit var inputValues: List<Any>

    @BeforeEach
    fun setUp() {
        val schema = Schema.Parser().parse(
            javaClass.getResourceAsStream("/com/github/event/Feed.avsc")
        )
        mockSchemaRegistryClient = MockSchemaRegistry.getClientForScope(registryScope)
        mockSchemaRegistryClient.register("$inputTopic-value", AvroSchema(schema))
        inputValues = listOf<Any>(GenericData.Record(schema).also {
            it.put("user", "Bernard")
            it.put("is_new", true)
            it.put("content", "Hello world!")
        })
    }

    @AfterEach
    fun tearDown() {
        MockSchemaRegistry.dropScope(registryScope)
    }

    @Test
    fun testStream() {
        val streamsBuilder = StreamsBuilder()
        val stream: KStream<String, GenericRecord> = streamsBuilder
            .stream<String, GenericRecord>(inputTopic)
        stream.to(outputTopic, Produced.with<String, GenericRecord>(Serdes.String(), genericAvroSerde))

        val topologyTestDriver = TopologyTestDriver(streamsBuilder.build(), streamsConfiguration)
        val input: TestInputTopic<String, Any> = topologyTestDriver
            .createInputTopic(
                inputTopic,
                Serdes.String().serializer(),
                KafkaAvroSerializer(mockSchemaRegistryClient)
            )
        val output: TestOutputTopic<String, Any> = topologyTestDriver
            .createOutputTopic(
                outputTopic,
                Serdes.String().deserializer(),
                KafkaAvroDeserializer(mockSchemaRegistryClient)
            )

        input.pipeValueList(inputValues)
        assertEquals(output.readValuesToList(), inputValues)
        topologyTestDriver.close()
    }
}
