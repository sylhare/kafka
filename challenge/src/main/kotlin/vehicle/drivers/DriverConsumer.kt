package vehicle.drivers

import com.fasterxml.jackson.databind.deser.std.StringDeserializer
import io.confluent.monitoring.clients.interceptor.MonitoringConsumerInterceptor
import org.apache.kafka.clients.consumer.ConsumerConfig.*
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.time.Duration
import java.util.*

class DriverConsumer {

    private val settings = Properties()

    init {
        settings[GROUP_ID_CONFIG] = "Consumer"
        settings[BOOTSTRAP_SERVERS_CONFIG] = BOOTSTRAP_SERVER
        settings[AUTO_OFFSET_RESET_CONFIG] = "earliest"
        settings[KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        settings[VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        settings[INTERCEPTOR_CLASSES_CONFIG] = listOf(MonitoringConsumerInterceptor::class.java)
    }

    var consumer = KafkaConsumer<String, String>(settings)

    fun consume() {
        try {
            consumer.subscribe(listOf(KAFKA_TOPIC))
            while (true) {
                val records = consumer.poll(Duration.ofMillis(100))
                records.forEach {
                    print("Key:${it.key()} Value:${it.value()} [partition ${it.partition()}]\n")
                }
            }
        } finally { // Clean up when the application exits or errors
            println("Closing consumer.")
            consumer.close()
        }
    }
}
