package spring.config

import org.apache.kafka.common.header.Headers
import org.springframework.kafka.support.serializer.JsonDeserializer
import spring.entity.Foo

class DiscardJsonDeserializer<T> : JsonDeserializer<Foo>() {
    private fun tryDeserialize(deserializer: () -> Foo, data: ByteArray?) = try {
        deserializer()
    } catch (e: Exception) {
        println("Invalid Request ${data?.let { String(it) }} ${e.message}")
        Foo.invalid
    }

    override fun deserialize(topic: String?, headers: Headers?, data: ByteArray?) =
        tryDeserialize({ super.deserialize(topic, headers, data) }, data)

    override fun deserialize(topic: String?, data: ByteArray?) =
        tryDeserialize({ super.deserialize(topic, data) }, data)
}