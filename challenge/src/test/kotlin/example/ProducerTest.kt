package example

import org.apache.kafka.clients.producer.MockProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ProducerTest {
  private lateinit var mockProducer: MockProducer<String, String>
  private val topic = "aTopic"
  private val producerRecord = ProducerRecord(topic, "key", "test")

  @BeforeEach
  fun setup() {
    mockProducer = MockProducer()
  }

  @Test
  fun producerSendsDataTest() {
    val exampleProducer = ExampleProducer(topic, mockProducer)
    exampleProducer.send(producerRecord)
    assertEquals(listOf(producerRecord), mockProducer.history())
  }
}