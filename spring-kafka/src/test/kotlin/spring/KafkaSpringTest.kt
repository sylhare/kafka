package spring

import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest(
    properties = [
        "spring.kafka.producer.bootstrap-servers=localhost:3333",
        "spring.kafka.consumer.bootstrap-servers=localhost:3333"
    ]
)
@EmbeddedKafka(
    topics = ["example.topic"],
    brokerProperties = [
        "advertised.listeners=PLAINTEXT://localhost:3333",
        "listeners=PLAINTEXT://localhost:3333",
        "port=3333",
        "log.dir=out/embedded-kafka"]
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
        val consumer: Consumer<String, String> = setupConsumer()

        fooProducer.send(Foo("foo", "bar"))

        val record: ConsumerRecord<String, String>? = null
        //    KafkaTestUtils.getSingleRecord<String, String>(consumer, "example.topic", 1000)
        println("Total records ${KafkaTestUtils.getRecords(consumer, 1000).count()}")
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
                "example.topic",
                "key",
                "{\"name\":\"mockFoo\", \"description\":\"mockBar\"}"
            )
        )
        producer.flush()
        println("Foo received: ${fooConsumer.foos}")
    }

    private fun setupConsumer(): Consumer<String, String> {
        val consumer: Consumer<String, String> = DefaultKafkaConsumerFactory(
            KafkaTestUtils.consumerProps("consumer", "false", embeddedKafkaBroker),
            StringDeserializer(),
            StringDeserializer()
        ).createConsumer()
        consumer.subscribe(listOf("example.topic"))
        return consumer
    }

    private fun setupProducer() = DefaultKafkaProducerFactory(
        KafkaTestUtils.producerProps(embeddedKafkaBroker),
        StringSerializer(),
        StringSerializer()
    ).createProducer()
}
