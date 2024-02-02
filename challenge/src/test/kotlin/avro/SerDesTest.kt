package avro

import com.github.event.Custom
import com.github.event.Example
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import org.apache.avro.specific.SpecificData
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class SerDesTest {

    private val mockSchemaRegistryClient = MockSchemaRegistryClient()
    private val registryUrl = "mock://registry"
    private val example = Example("hello", Custom(true))
    private val serializer = KafkaAvroSerializer(mockSchemaRegistryClient)
    private val deserializer = KafkaAvroDeserializer(mockSchemaRegistryClient)

    @BeforeEach
    fun setup() {
        serializer.configure(mapOf(
            KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG to registryUrl,
            KafkaAvroDeserializerConfig.AUTO_REGISTER_SCHEMAS to true
        ), false)
        deserializer.configure(mapOf(
            KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG to registryUrl,
            KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG to true // make it like a SpecificAvroDeserializer
        ), false)

        Pair("Example", Example.`SCHEMA$`)
    }

    @Test
    fun avroDeserialization() {
        val specificBytes = serializer.serialize("topic", example)
        val deserialized = deserializer.deserialize("topic", specificBytes)
        assertTrue(deserialized is Example)
        assertEquals(example, deserialized)
    }

    @Test
    fun avroSerDesSpecificToGeneric() {
        val specificSerializer = SpecificAvroSerde<Example>().apply {
            configure(mapOf(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG to registryUrl), false)
        }.serializer()
        val genericDeserializer = GenericAvroSerde().apply {
            configure(mapOf(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG to registryUrl), false)
        }.deserializer()

        // Avro serializer with specific serializer
        val specificBytes = specificSerializer.serialize("topic", example)
        // Obtained generic record from deserializer
        val deserialized = genericDeserializer.deserialize("topic", specificBytes)
        // Cast from a generic record to a specific record using generated class from avro schema
        val deserializedExample = SpecificData.get().deepCopy(Example.`SCHEMA$`, deserialized)
        assertEquals(deserialized.schema.name, Example.getClassSchema().name)
        assertTrue(deserializedExample is Example)
        assertEquals(example, deserializedExample)
    }
}
