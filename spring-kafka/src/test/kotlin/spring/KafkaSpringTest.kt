package spring

import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.annotation.DirtiesContext
import spring.bus.FooConsumer
import spring.bus.FooProducer
import spring.entity.Foo
import java.util.*


@SpringBootTest(
    properties = [
        "spring.kafka.producer.bootstrap-servers=localhost:3333",
        "spring.kafka.consumer.bootstrap-servers=localhost:3333"
    ]
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // To reset kafka in between tests
@EmbeddedKafka(
    topics = ["producer.topic", "consumer.topic"],
    partitions = 1,
    controlledShutdown = false,
    brokerProperties = [
        "advertised.listeners=PLAINTEXT://localhost:3333",
        "listeners=PLAINTEXT://localhost:3333",
        "port=3333",
        "log.dir=out/embedded-kafka"] // Where the embedded kafka writes the logs (gets deleted after)
)
internal class KafkaSpringTest {

    @Autowired
    private lateinit var embeddedKafkaBroker: EmbeddedKafkaBroker

    @Autowired
    private lateinit var fooConsumer: FooConsumer

    @Autowired
    private lateinit var fooProducer: FooProducer

    @Test
    fun selfProduceConsumeTest() {
        println("\n------- Self Produce Consume Test -------")
        val consumer: Consumer<String, String> = setupTestConsumer()
        val producer: Producer<String, String> = setupTestProducer()

        println("Sending \"my-test-value\"")
        producer.send(ProducerRecord("producer.topic", "123", "my-test-value"))

        val singleRecord = KafkaTestUtils.getSingleRecord(consumer, "producer.topic")
        assertEquals("my-test-value", singleRecord.value())
        assertEquals("123", singleRecord.key())

        consumer.close()
        producer.close()
    }

    @Test
    fun producerTest() {
        println("\n------- producer Test -------")
        val consumer: Consumer<String, String> = setupTestConsumer()

        fooProducer.send(Foo("foo", "bar"))
        Thread.sleep(500)
        val record: ConsumerRecord<String, String> = KafkaTestUtils.getSingleRecord<String, String>(consumer, "producer.topic", 1000)
        println("Record received $record")
        assertEquals("key", record.key())

        consumer.close()
    }

    @Test
    fun consumeTest() {
        println("\n------- Consumer Test -------")
        val producer: Producer<String, String> = setupTestProducer()

        println("Sending \"{\"name\":\"mockFoo\", \"description\":\"mockBar\"}\"")
        producer.send(ProducerRecord("consumer.topic", "key2", "{\"name\":\"mockFoo\", \"description\":\"mockBar\"}"))
        producer.flush()
        Thread.sleep(500)
        assertEquals(Foo("mockFoo", "mockBar"), fooConsumer.foos.first())

        producer.close()
    }
    
    private fun setupTestConsumer(): Consumer<String, String> {
        val config = KafkaTestUtils.consumerProps("${UUID.randomUUID()}", "false", embeddedKafkaBroker)
        config[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        val consumer: Consumer<String, String> = DefaultKafkaConsumerFactory(
            config,
            StringDeserializer(),
            StringDeserializer()
        ).createConsumer()
        consumer.subscribe(listOf("producer.topic"))
        return consumer
    }

    private fun setupTestProducer() = DefaultKafkaProducerFactory(
        KafkaTestUtils.producerProps(embeddedKafkaBroker),
        StringSerializer(),
        StringSerializer()
    ).createProducer()
}
