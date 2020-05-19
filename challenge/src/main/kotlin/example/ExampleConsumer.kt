package example

import org.apache.kafka.clients.consumer.Consumer
import java.time.Duration
import kotlin.concurrent.thread

class ExampleConsumer(topic: String, private val consumer: Consumer<String, String>, private val service: Service) {

  private var running: Boolean = true

  fun subscribe(topic: String) = consumer.subscribe(listOf(topic))
  fun poll() {
    thread {
      println("Listening to kafka")
      while (running) {
        consumer.poll(Duration.ofMillis(100)).forEach {
          println("Handling ${it.value()}")
          service.handle(it.value())
        }
      }
    }
  }

  fun stop() {
    running = false
    println("Stopping")
  }
}
