import kafka.server.KafkaConfig
import kafka.server.KafkaServerStartable
import org.apache.curator.test.TestingServer
import java.io.IOException
import java.util.*

/**
 * @Deprecated: Compatible with kafka v2.5.0
 *
 *   // Deprecated: For KafkaTestFixture
 *   testImplementation("org.apache.curator:curator-test:2.8.0")
 *   testImplementation("org.apache.kafka:kafka_2.10:0.8.2.1")
 */
internal class KafkaTestFixture {
  private lateinit var zk: TestingServer
  private lateinit var kafka: KafkaServerStartable

  @Throws(Exception::class)
  fun start(properties: Properties) {
    val port = getZkPort(properties)
    zk = TestingServer(port)
    zk.start()
    val kafkaConfig = KafkaConfig(properties)
    kafka = KafkaServerStartable(kafkaConfig)
    kafka.startup()
  }

  @Throws(IOException::class)
  fun stop() {
    kafka.shutdown()
    zk.stop()
    zk.close()
  }

  private fun getZkPort(properties: Properties): Int {
    val url = properties["zookeeper.connect"] as String?
    val port = url!!.split(":").toTypedArray()[1]
    return Integer.valueOf(port)
  }
}
