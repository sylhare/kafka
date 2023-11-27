package stream;

import java.util.Properties;

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KafkaStreams.State;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.subject.RecordNameStrategy;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;

/**
 * https://github.com/findinpath/kafka-streams-generic-avro/blob/master/src/main/java/com/findinpath/GenericRecordTopology.java
 * Simple kafka-streams stateless topology used for showcasing on how to consume
 * several event types from the same input topic.
 * <p>
 * The events coming from the input topic will be deserialized as `GenericRecord` objects.
 */
public class GenericRecordTopology implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericRecordTopology.class);

    private final KafkaStreams streams;

    private KafkaStreams.State state;

    public GenericRecordTopology(String bootstrapServers, String schemaRegistryUrl, String applicationId, String inputTopic, String outputTopic) {
        final Properties streamsConfiguration = createStreamsConfiguration(bootstrapServers, schemaRegistryUrl, applicationId);

        final StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, GenericRecord> records = builder.stream(inputTopic);

        records.peek((key, record) -> LOGGER.info("Processing entry with the key " + key + " and value " + record)).to(outputTopic);

        streams = new KafkaStreams(builder.build(), streamsConfiguration);
        streams.setStateListener((newState, oldState) -> state = newState);
    }

    public void start() {
        LOGGER.info("Starting topology");
        streams.start();

        // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    public void close() {
        LOGGER.info("Closing topology");
        streams.close();
    }

    public State getState() {
        return state;
    }

    private Properties createStreamsConfiguration(String bootstrapServers, String schemaRegistryUrl, String applicationId) {
        final Properties streamsConfiguration = new Properties();
        // Give the Streams application a unique name.  The name must be unique in the Kafka cluster
        // against which the application is run.
        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        // Where to find Kafka broker(s).
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        streamsConfiguration.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        // Specify default (de)serializers for record keys and for record values.
        streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, GenericAvroSerde.class);
        streamsConfiguration.put(AbstractKafkaAvroSerDeConfig.VALUE_SUBJECT_NAME_STRATEGY, RecordNameStrategy.class);

        return streamsConfiguration;
    }
}
