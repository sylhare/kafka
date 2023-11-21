package tram

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.serialization.Serializer

class JsonSerializer<T> : Serializer<T> {
  private val objectMapper: ObjectMapper = ObjectMapper()
  override fun configure(configs: Map<String?, *>?, isKey: Boolean) {}
  override fun serialize(topic: String, data: T): ByteArray = try {
    objectMapper.writeValueAsBytes(data)
  } catch (e: JsonProcessingException) {
    throw SerializationException(e)
  }

  override fun close() {}
}
