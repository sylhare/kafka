package spring.service

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import spring.entity.Foo

@Profile("!kafka-test")
@Service
class FooService {

    fun handle(foo: Foo) {
        println("Handling foo: $foo")
    }
}
