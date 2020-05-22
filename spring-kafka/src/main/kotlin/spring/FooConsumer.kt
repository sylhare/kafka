package spring

import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
class FooConsumer {

    val foos = mutableListOf<Foo>()
    @KafkaListener(
        id = "foo-consumer",
        topics = ["\${app.topic.consumer}"]
    )

    fun consume(@Payload foo: Foo) {
        println("Consuming Request: $foo")
        foos.add(foo)
        println(foos)
    }


}