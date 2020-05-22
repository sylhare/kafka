package spring

import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.context.junit4.SpringRunner
import java.util.*

@RunWith(SpringRunner::class)
@SpringBootTest(
    properties = [
        "spring.kafka.producer.bootstrap-servers=localhost:3333",
        "spring.kafka.consumer.bootstrap-servers=localhost:3333"
    ]
)
@EmbeddedKafka(
    topics = ["producer.topic", "consumer.topic"],
    partitions = 1,
    brokerProperties = [
        "advertised.listeners=PLAINTEXT://localhost:3333",
        "listeners=PLAINTEXT://localhost:3333",
        "port=3333",
        "log.dir=out/embedded-kafka"] // Where the embedded kafka writes the logs (gets deleted after)
)
internal class BarringTest {

    @Autowired
    private lateinit var embeddedKafkaBroker: EmbeddedKafkaBroker

    @Autowired
    private lateinit var fooConsumer: FooConsumer

    @Autowired
    private lateinit var fooProducer: FooProducer

    @Test
    fun producerTest() {
        println("\nproducer Test")
        val mockConsumer: Consumer<String, String> = setupMockConsumer()

        fooProducer.send(Foo("foo", "bar"))

        val record: ConsumerRecord<String, String>? = null
        //    KafkaTestUtils.getSingleRecord<String, String>(consumer, "producer.topic", 1000)
        println("Total records ${KafkaTestUtils.getRecords(mockConsumer, 1000).count()}")
        println("Record received $record")
        //assertEquals("{\"name\":\"foo\", \"description\":\"bar\"}", record.value())
    }

    @Test
    fun consumerTest() {
        println("\nConsumer Test")
        val producer: Producer<String, String> = setupProducer()
        println("Sending \"{\"name\":\"mockFoo\", \"description\":\"mockBar\"}\"")
        producer.send(
            ProducerRecord(
                "consumer.topic",
                "key",
                "{\"name\":\"mockFoo\", \"description\":\"mockBar\"}"
            )
        )
        producer.flush()
        println("Foo received: ${fooConsumer.foos}")
    }

    private fun setupMockConsumer(): Consumer<String, String> {
        val config = KafkaTestUtils.consumerProps("consumer", "false", embeddedKafkaBroker)
        config[ConsumerConfig.GROUP_ID_CONFIG] = UUID.randomUUID().toString()
        config[ConsumerConfig.CLIENT_ID_CONFIG] = "your_client_id"
        config[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        val consumer: Consumer<String, String> = DefaultKafkaConsumerFactory(
            config,
            StringDeserializer(),
            StringDeserializer()
        ).createConsumer()
        consumer.subscribe(listOf("producer.topic"))
        return consumer
    }

    private fun setupProducer() = DefaultKafkaProducerFactory(
        KafkaTestUtils.producerProps(embeddedKafkaBroker),
        StringSerializer(),
        StringSerializer()
    ).createProducer()
}
