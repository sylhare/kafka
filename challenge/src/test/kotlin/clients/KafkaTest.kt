package clients

import kafka.consumer.Consumer
import kafka.consumer.ConsumerConfig
import kafka.consumer.ConsumerIterator
import kafka.consumer.KafkaStream
import kafka.javaapi.consumer.ConsumerConnector
import kafka.serializer.StringDecoder
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.consumer.MockConsumer
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap
import kotlin.test.assertEquals

class KafkaTest {
  private val topic = "topic1-" + System.currentTimeMillis()

  private lateinit var server: KafkaTestFixture
  private lateinit var producer: KafkaProducer<String, String>
  private lateinit var consumer: MockConsumer<String, String>
  private lateinit var consumerConnector: ConsumerConnector

  @BeforeEach
  @Throws(Exception::class)
  fun setup() {
    server = KafkaTestFixture()
    server.start(serverProperties())
  }

  @AfterEach
  @Throws(java.lang.Exception::class)
  fun teardown() {
    producer.close()
    consumerConnector.shutdown()
    server.stop()
  }

  @Disabled
  @Test
  @Throws(java.lang.Exception::class)
  fun createMockConsumer() { //Create a consumer
    consumer = MockConsumer<String, String>(OffsetResetStrategy.EARLIEST)
  }

  @Test
  @Throws(IOException::class)
  fun testConsumer() { // This is YOUR consumer object
    val myTestConsumer = MyTestConsumer()
    // Inject the MockConsumer into your consumer
// instead of using a KafkaConsumer
    myTestConsumer.consumer = (consumer as KafkaConsumer<String, String>)
    consumer.assign(listOf(TopicPartition("my_topic", 0)))

    val beginningOffsets: HashMap<TopicPartition, Long> = HashMap()
    beginningOffsets[TopicPartition("my_topic", 0)] = 0L
    consumer.updateBeginningOffsets(beginningOffsets)
    consumer.addRecord(ConsumerRecord("my_topic", 0, 0L, "mykey", "myvalue0"))
    consumer.addRecord(ConsumerRecord("my_topic", 0, 1L, "mykey", "myvalue1"))
    consumer.addRecord(ConsumerRecord("my_topic", 0, 2L, "mykey", "myvalue2"))
    consumer.addRecord(ConsumerRecord("my_topic", 0, 3L, "mykey", "myvalue3"))
    consumer.addRecord(ConsumerRecord("my_topic", 0, 4L, "mykey", "myvalue4"))

    // This is where you run YOUR consumer's code
    // This code will consume from the Consumer and do your logic on it
    myTestConsumer.consume()
    // This just tests for exceptions
    // Somehow test what happens with the consume()
  }

  @Disabled
  @Test
  @Throws(java.lang.Exception::class)
  fun shouldWriteThenRead() { //Create a consumer
    val it = buildConsumer(topic)
    //Create a producer
    producer = KafkaProducer(producerProps())
    //send a message
    producer.send(ProducerRecord<String, String>(topic, "message")).get()
    //read it back
    val messageAndMetadata = it!!.next()
    val value = messageAndMetadata.message()
    assertEquals("message", value)
  }

  private fun buildConsumer(topic: String): ConsumerIterator<String, String>? {
    val props: Properties = consumerProperties()
    val topicCountMap: MutableMap<String, Int> = HashMap()
    topicCountMap[topic] = 1
    val consumerConfig = ConsumerConfig(props)
    consumerConnector = Consumer.createJavaConsumerConnector(consumerConfig)
    val consumers: Map<String, List<KafkaStream<String, String>>> = consumerConnector.createMessageStreams(topicCountMap, StringDecoder(null), StringDecoder(null))
    val stream = consumers[topic]!![0]
    return stream.iterator()
  }

  private fun consumerProperties(): Properties {
    val props = Properties()
    props["zookeeper.connect"] = serverProperties()["zookeeper.connect"]
    props["group.id"] = "group1"
    props["auto.offset.reset"] = "smallest"
    return props
  }

  private fun producerProps(): Properties? {
    val props = Properties()
    props["bootstrap.servers"] = "localhost:9092"
    props["key.serializer"] = StringDeserializer::class.java
    props["value.serializer"] = StringDeserializer::class.java
    props["request.required.acks"] = "1"
    return props
  }

  private fun serverProperties(): Properties {
    val props = Properties()
    props["zookeeper.connect"] = "localhost:2181"
    props["broker.id"] = "1"
    return props
  }
}