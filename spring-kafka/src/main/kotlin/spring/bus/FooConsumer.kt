package spring.bus

import org.springframework.kafka.annotation.KafkaHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import spring.entity.Foo

@KafkaListener(
    //id = "foo-consumer",
    groupId = "foo-group",
    topics = ["\${app.topic.consumer}"]
)
@Component
class FooConsumer {

    val foos = mutableListOf<Foo>()

    @KafkaHandler
    fun consume(@Payload foo: Foo) {
        println("Consuming Request: $foo")
        foos.add(foo)
        println("All recieved: $foos")
    }

}