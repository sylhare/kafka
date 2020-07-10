package spring.bus

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import spring.entity.Foo

@Component
class FooProducer {

    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<String, Foo>

    @Value("\${app.topic.producer}")
    private lateinit var topic: String

    fun send(@Payload data: Foo) {
        println("sending data:$data to topic:'$topic'")
        kafkaTemplate.send(topic, "key", data)
        println("Foo should be sent")
    }
}
