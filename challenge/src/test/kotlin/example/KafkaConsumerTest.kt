package example

import example.mock.MockService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.MockConsumer
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.apache.kafka.common.TopicPartition
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ConsumerTest {

  private lateinit var mockConsumer: MockConsumer<String, String>
  private val request = "test"
  private val topic = "aTopic"
  private val partition = 1
  private val consumerRecord = ConsumerRecord(topic, partition, 0, "key", request)

  @BeforeEach
  fun setup() {
    MockConsumer<String, String>(OffsetResetStrategy.EARLIEST)
  }

  @Test
  fun subscribeConsumerTest() {
    val mockService = MockService()
    val exampleConsumer = ExampleConsumer(topic, mockConsumer, mockService)
    exampleConsumer.subscribe(topic)
    mockConsumer.rebalance(listOf(TopicPartition(topic, partition)))
    mockConsumer.updateBeginningOffsets(mapOf(TopicPartition(topic, partition) to 0L))
    exampleConsumer.poll()
    mockConsumer.addRecord(consumerRecord)
    Thread.sleep(500)
    exampleConsumer.stop()
    assertEquals(mockService.receivedRequest, request)
  }

  @Test
  fun noSubscribeConsumerTest() {
    val mockService = MockService()
    val exampleConsumer = ExampleConsumer(topic, mockConsumer, mockService)
    mockConsumer.assign(listOf(TopicPartition(topic, partition)))
    mockConsumer.updateBeginningOffsets(mapOf(TopicPartition(topic, partition) to 0L))
    exampleConsumer.poll()
    mockConsumer.addRecord(consumerRecord)
    Thread.sleep(500)
    exampleConsumer.stop()
    assertEquals(mockService.receivedRequest, request)
  }
}