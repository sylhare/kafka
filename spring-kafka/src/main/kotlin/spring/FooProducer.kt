package spring

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service

@Service
class FooProducer {

  @Autowired
  private lateinit var kafkaTemplate: KafkaTemplate<String, Foo>

  @Value("\${app.topic.example}")
  private val topic: String? = null

  fun send(data: Foo) {
    print("sending data=$data to topic='$topic'")
    val message: Message<Foo> = MessageBuilder
        .withPayload(data)
        .setHeader(KafkaHeaders.TOPIC, topic)
        .build()
    kafkaTemplate.send(message)
  }
}
