package spring.kafka.consumers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import spring.entity.Foo
import spring.service.FooService

@Component
class FooListener {

    @Autowired
    lateinit var fooService: FooService

    @KafkaListener(
        groupId = "foo-listener-group",
        topics = ["\${app.topic.consumer}"],
        containerFactory = "fooKafkaListenerContainerFactory"
    )
    fun consume(foo: Foo) {
        fooService.handle(foo);
    }
}
