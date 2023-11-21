package vehicle.position.avro.transformer

import io.confluent.kafka.serializers.KafkaJsonDeserializer
import io.confluent.kafka.serializers.KafkaJsonSerializer
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Produced
import vehicle.position.avro.VehiclePosition
import java.util.*

object VehiclePositionTransformer {
  @JvmStatic
  fun main(args: Array<String>) {
    println(">>> Starting the vp-streams-app Application")

    val topology = topology
    val streams = KafkaStreams(topology, settings)
    Runtime.getRuntime().addShutdownHook(Thread(Runnable {
      println("<<< Stopping the vp-streams-app Application")
      streams.close()
    }))
    streams.start()
  }

  private val settings: Properties get(){
    val settings = Properties()
    settings[StreamsConfig.APPLICATION_ID_CONFIG] = "vp-streams-app"
    settings[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "kafka:9092"
    return settings
  }

  private val topology: Topology
    get() {
      val stringSerde = Serdes.String()
      val vpSerde = jsonSerde
      val builder = StreamsBuilder()
      val positions = builder.stream("vehicle-positions", Consumed.with(stringSerde, vpSerde))
      val operator47Only = positions.filter { _: String?, value: VehiclePosition -> value.VP!!.oper == 47 }
      operator47Only.to("vehicle-positions-oper-47", Produced.with(stringSerde, vpSerde))
      return builder.build()
    }

  private val jsonSerde: Serde<VehiclePosition>
    get() {
      val serdeProps: MutableMap<String, Any?> = HashMap()
      serdeProps["json.value.type"] = VehiclePosition::class.java
      val vpSerializer: Serializer<VehiclePosition> = KafkaJsonSerializer()
      vpSerializer.configure(serdeProps, false)
      val vpDeserializer: Deserializer<VehiclePosition> = KafkaJsonDeserializer()
      vpDeserializer.configure(serdeProps, false)
      return Serdes.serdeFrom(vpSerializer, vpDeserializer)
    }
}
