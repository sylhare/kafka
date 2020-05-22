package spring

import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertEquals

@RunWith(SpringRunner::class)
@SpringBootTest(
    properties = []
)
@EmbeddedKafka(
    topics = ["example.topic"],
    brokerProperties = [
      "advertised.listeners=PLAINTEXT://localhost:9092",
      "listeners=PLAINTEXT://localhost:9092",
      "port=9092",
      "log.dir=out/embedded-kafka"]
)
internal class BarringTest {

  @Autowired
  private lateinit var embeddedKafkaBroker: EmbeddedKafkaBroker

  @Autowired
  private lateinit var fooProducer: FooProducer

  @Test
  fun producerTest() {
    val consumer: Consumer<String, String> = setupConsumer()

    fooProducer.send(Foo("foo", "bar"))

    val record: ConsumerRecord<String, String> =
        KafkaTestUtils.getSingleRecord<String, String>(consumer, "example.topic", 1000)
    assertEquals("{\"name\":\"foo\", \"description\":\"bar\"}", record.value())
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
}
