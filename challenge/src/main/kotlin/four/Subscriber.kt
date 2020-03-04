package four

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.util.*
import kotlin.system.exitProcess

class Subscriber(private var producer: KafkaProducer<String, String>) : MqttCallback {

  private val mqttQOS = 1
  private val host = "ssl://mqtt.hsl.fi:8883"
  private val clientId = "MQTT-Example"
  private val mqttTopic = "/hfp/v2/journey/ongoing/vp/#"
  private val kafkaTopic = "vehicle-positions"
  private lateinit var client: MqttClient

  @Throws(MqttException::class)
  override fun messageArrived(topic: String?, message: MqttMessage) {
    println(String.format("[%s] %s", topic, String(message.payload)))
    val key = topic
    val value = String(message.payload)
    val record = ProducerRecord(kafkaTopic, key, value)
    producer.send(record)
  }

  override fun connectionLost(cause: Throwable) {
    println("Connection lost because: $cause")
    exitProcess(1)
  }

  override fun deliveryComplete(token: IMqttDeliveryToken?) {
    println("delivery completed")
  }

  @Throws(MqttException::class)
  fun start() {
    val conOpt = MqttConnectOptions()
    conOpt.isCleanSession = true
    val uuid = UUID.randomUUID().toString().replace("-", "")
    val clientId = "$clientId-$uuid"
    client = MqttClient(host, clientId, MemoryPersistence())
    client.setCallback(this)
    client.connect(conOpt)
    client.subscribe(mqttTopic, mqttQOS)
  }
}