package spring

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import spring.entity.Foo
import spring.kafka.FooProducer
import spring.service.FooService

@ActiveProfiles(profiles = ["kafka-test"])
@SpringBootTest(
    properties = [
        "spring.kafka.producer.bootstrap-servers=localhost:3392",
        "spring.kafka.consumer.bootstrap-servers=localhost:3392",
        "app.topic.consumer=test-topic",
        "app.topic.producer=test-topic"
    ]
)
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = ["listeners=PLAINTEXT://localhost:3392", "port=3392"])
internal class FooListenerTest {

    @TestConfiguration
    open class TestConfig {
        @Bean
        open fun fooService() = mockk<FooService>()
    }

    @Autowired
    private lateinit var fooService: FooService

    @Autowired
    private lateinit var fooProducer: FooProducer


    @Test
    fun consumeFoo() {
        every { fooService.handle(any()) } just runs
        val foo = Foo("example", "description")
        fooProducer.send(foo)
        verify(timeout = 1000, atMost = 5) { fooService.handle(foo) }
    }
}
