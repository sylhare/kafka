package spring

import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer

data class Foo(private val name: String, private val description: String) {
  constructor() : this("default", "default")
}

@Configuration
open class Config {

  @Autowired
  private lateinit var kafkaProperties: KafkaProperties

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