package spring.config

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer
import spring.entity.Foo
import spring.entity.Foo.Companion.invalid

@Configuration
open class Config {

    @Autowired
    private lateinit var kafkaProperties: KafkaProperties

    private fun consumerConfigs(): MutableMap<String, Any> {
        val props: MutableMap<String, Any> = kafkaProperties.buildConsumerProperties()
        props[JsonSerializer.ADD_TYPE_INFO_HEADERS] = false
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = DiscardJsonDeserializer::class.java
        return props
    }

    private fun consumerFactory() =
        DefaultKafkaConsumerFactory(consumerConfigs(), StringDeserializer(),
            DiscardJsonDeserializer<Foo>()
        )

    @Bean(name=["fooKafkaListenerContainerFactory"])
    open fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, Foo>? {
        val factory: ConcurrentKafkaListenerContainerFactory<String, Foo> = ConcurrentKafkaListenerContainerFactory()
        factory.consumerFactory = consumerFactory()
        return factory
    }

    @Bean
    open fun producerConfigs(): ProducerFactory<String, Foo> {
        val props: MutableMap<String, Any?> = HashMap(kafkaProperties.buildProducerProperties())
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
        return DefaultKafkaProducerFactory(props)
    }

    @Bean
    open fun kafkaTemplate(): KafkaTemplate<String, Foo> {
        return KafkaTemplate(producerConfigs(), true)
    }
}