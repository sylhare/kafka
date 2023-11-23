package example

import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord

class ExampleProducer(private val producer: Producer<String, String>) {
  fun send(producerRecord: ProducerRecord<String, String>) {
    producer.send(producerRecord)
  }
}
