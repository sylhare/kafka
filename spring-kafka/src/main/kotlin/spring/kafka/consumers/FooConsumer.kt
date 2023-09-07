package spring.kafka.consumers

import org.springframework.kafka.annotation.KafkaHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import spring.entity.Foo

@KafkaListener(
    //id = "foo-consumer",
    groupId = "foo-group",
    topics = ["\${app.topic.consumer}"],
    containerFactory = "fooKafkaListenerContainerFactory"
)
@Component
class FooConsumer {

    val foos = mutableListOf<Foo>()

    @KafkaHandler
    fun consume(@Header(KafkaHeaders.RECEIVED_TIMESTAMP) received: Long, @Payload foo: Foo) {
        println("Consuming Request: $foo received at $received")
        foos.add(foo)
        println("All received: $foos")
    }

}
