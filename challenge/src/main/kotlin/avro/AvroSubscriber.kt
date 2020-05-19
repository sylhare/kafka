package avro

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import avro.model.PositionKey
import avro.model.PositionValue
import java.io.IOException
import java.util.*
import kotlin.system.exitProcess

class AvroSubscriber(private var producer: KafkaProducer<PositionKey, PositionValue>) : MqttCallback {

  private val qos = 1
  private val host = "ssl://mqtt.hsl.fi:8883"
  private val clientId = "MQTT-Avro-Example"
  private val mqttTopic = "/hfp/v2/journey/ongoing/vp/#"
  private val kafkaTopic = "vehicle-positions-avro"
  private var client: MqttClient? = null

  @Throws(MqttException::class)
  fun start() {
    val conOpt = MqttConnectOptions()
    conOpt.isCleanSession = true
    val uuid = UUID.randomUUID().toString().replace("-", "")
    val clientId = "$clientId-$uuid"
    client = MqttClient(host, clientId, MemoryPersistence())
    client!!.setCallback(this)
    client!!.connect(conOpt)
    client!!.subscribe(mqttTopic, qos)
  }

  override fun connectionLost(cause: Throwable) {
    println("Connection lost because: $cause")
    exitProcess(1)
  }

  override fun deliveryComplete(token: IMqttDeliveryToken?) {}

  @Throws(MqttException::class)
  override fun messageArrived(topic: String?, message: MqttMessage) {
    try {
      println("[$topic] ${String(message.payload)}")
      val key = PositionKey(topic)
      val value = getPositionValue(message.payload)
      val record = ProducerRecord(kafkaTopic, key, value)
      producer.send(record)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  @Throws(IOException::class)
  private fun getPositionValue(payload: ByteArray): PositionValue {
    val mapper = ObjectMapper()
    val json = String(payload)
    val pos: VehiclePosition = mapper.readValue(json, VehiclePosition::class.java)
    val vv: VehiclePosition.VehicleValues = pos.VP!!
    return PositionValue(vv.desi, vv.dir, vv.oper, vv.veh, vv.tst, vv.tsi, vv.spd, vv.hdg, vv.lat,
        vv.longitude, vv.acc, vv.dl, vv.odo, vv.drst, vv.oday, vv.jrn, vv.line, vv.start, vv.loc,
        vv.stop, vv.route, vv.occu, vv.seq)
  }
}