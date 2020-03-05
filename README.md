# Confluent Apache Kafka for Developers

Source code and example on how to use Kafka for developers by Confluent.

## Introduction to Kafka

### Topics

Producer writes to a `topic`.
Consumers polls and reads the messages from `topic`.
Messages can be read multiple times depending on your retention policy. (Which offsets when you are going to read from) 

### with Broker

`Topics` are more efficient when they are on multiple `partitions`. 
Those `partitions` are on one or multiple `brokers`, which are used for resilience, because if one `broker` goes done the messages
written / read on the `Topic` will still be able to be processed by other `brokers`.

With no keys the messages or record will be sent to the partition using round robin. If there is a key, 
it will use this key to define the partition example:
```
5 partition, message with key = 7 -> <key> mod( <partition> ) = 2 
> Which mean message of key 7 will go to partition 2 in this case.
```

You can set a replica factor so that you will have a replica of each of the Topic partition in the other brokers.
Ideally you set it to three, those wil create ISR `in sync replicas`.

The data retention is made with the segments which are files that stores/logs the events that have been sent on the partition.
Messages that are read are said to be `committed` to the log. So we know which message has been listened to and when.
If a consumer fails to read a message, another consumer can review the log to start back at the right event in the queue.



## Running

Make sure to add an extra 6Go to your docker to run it.

```bash
# Build
docker-compose up -d
# Execute commands
docker-compose exec tools /bin/bash
cd ~/confluent-dev/labs/
gradle run
# Remove
docker-compose down -v
```

Don't forget to add in your `/etc/hosts` the following:
```
127.0.0.1       localhost kafka training avro schema-registry
```

## Configuration Elements

ACK -> Acknowledge. There are three settings :
  - `acks=0` will not wait for the acknowledgement of the server
  - `acks=1` will wait for the leader to write on local log
  - `ack=all` Producer will wait for all in sync replicas (ISR) to have acknowledged the receipt of the record within delivery time

The `retries` for the amount of time it will retry (Until `MAX_INT`).
You can set a `retry.backoff.ms` to pause in between retries. (default to 100ms)    

The `delivery.timeout.ms` puts a limit to report the result or failure from a producer:
send() - batching - awaits send - retry - back off 


## Data Format
### Definition
Kafka don't do well with big files `message.max.bytes` is recommended to 1mb.
Plain text is not very efficient, everything needs to be converted to text.
We want to use a schema to structure the data.

Avro (an other apache open source project) used for serialization of the data.
It's like an optimized json, faster to process and more robust.

### Implementation
To generate automatically the classes for avro you can use the [gradle-avro-plugin](https://github.com/davidmc24/gradle-avro-plugin).
Once you have your schemes in `./src/main/avro`. 
In your `build.gradle.kts` you need to add:
```kotlin
plugins {
    id("com.commercehub.gradle.plugin.avro") version "0.9.1"
}
repositories {
    maven ("https://dl.bintray.com/gradle/gradle-plugins")
}
```

Then you can run the task that will create the classes then build using:
```bash
gradle generateAvroJava build
```

You will need to register the Avro schemas to the registry:
```bash
cd ~/confluent-dev/labs/src/main/resources/schemas/
# Register the schema
curl -X POST \
-H "Content-Type: application/vnd.schemaregistry.v1+json" \
--data @sample-schema-v2.json \
schema-registry:8081/subjects/sample/versions
#
```

### Stream and table

Check out more on [Confluent: Kafka Streams and Tables](https://www.confluent.io/blog/kafka-streams-tables-part-1-event-streaming/).
`Stream` represents an history of events and are immutable data.
`Table` represents a state and are mutable.

Watch the transformed stream.
```bash
kafka-console-consumer \
--bootstrap-server kafka:9092 \
--topic vehicle-positions-oper-47 \
--from-beginning
```

## Topics

### Create topic

> Do not create topics starting with `_` which is for offsets topics.

```bash
# create / modify
# --partitions Number of partition for the topic
# --replication-factor For durability, default is 1
kafka-topics \
--create \
--bootstrap-server kafka:9092 \
--partitions 6 \
--replication-factor 1 \
--topic vehicle-positions
```