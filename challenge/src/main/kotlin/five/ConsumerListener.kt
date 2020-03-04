package five

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.TopicPartition
import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

class ConsumerListener(private val consumer: KafkaConsumer<String, String>) : ConsumerRebalanceListener {

  override fun onPartitionsAssigned(partitions: MutableCollection<TopicPartition>) {
    // Do nothing
  }

  override fun onPartitionsRevoked(partitions: MutableCollection<TopicPartition>) {
    consumer.seekToBeginning(partitions)
  }
}

// Manually using offset
fun createListener(consumer: KafkaConsumer<String, String>): ConsumerRebalanceListener {
  return object : ConsumerRebalanceListener {
    override fun onPartitionsRevoked(partitions: Collection<TopicPartition?>?) {
      // nothing to do...
    }

    override fun onPartitionsAssigned(partitions: Collection<TopicPartition>) {
      for (partition in partitions) {
        try {
          if (Files.exists(Paths.get(OFFSET_FILE_PREFIX + partition.partition()))) {
            val offset: Long = Files.readAllLines(
                Paths.get(OFFSET_FILE_PREFIX + partition.partition()),
                Charset.defaultCharset())[0].toLong()
            consumer.seek(partition, offset)
          }
        } catch (e: IOException) {
          println("ERR: Could not read offset from file.\n")
        }
      }
    }
  }
}