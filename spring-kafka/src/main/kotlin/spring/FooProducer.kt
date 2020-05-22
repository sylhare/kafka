package spring

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.Message
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

@Component
class FooProducer {

  @Autowired
  private lateinit var kafkaTemplate: KafkaTemplate<String, Foo>

  @Value("\${app.topic.producer}")
  private lateinit var topic: String

  fun send(@Payload data: Foo) {
    println("sending data=$data to topic='$topic'")
    kafkaTemplate.send(topic, "key", data)
  }
}
