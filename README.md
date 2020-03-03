# Confluent Apache Kafka for Developers Course

This is the source code accompanying the **Confluent Apache Kafka for Developers** course.

It is organized in two subfolders `solution` and `labs`. 
The former contains the complete sample solution for each exercise whilst the latter contains the scaffoliding for 
each exercise and is meant to be used and elaborated on by the students during the hands-on.

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

## Configuration Elements

ACK -> Acknowledge. There are three settings :
  - `acks=0` will not wait for the acknowledgement of the server
  - `acks=1` will wait for the leader to write on local log
  - `ack=all` Producer will wait for all in sync replicas to have acknowledged the receipt of the record

The `retries` for the amount of time it will retry (Until `MAX_INT`).
You can set a `retry.backoff.ms` to pause in between retries. (default to 100ms)    

The `delivery.timeout.ms` puts a limit to report the result or failure from a producer:
send() - batching - awaits send - retry - back off 

## Data

Avro (an other apache open source project) used for serialization of the data.
It's like an optimized json, faster to process and more robust.

## Topics

### Create topic

> Do not create topics starting with `_` which is for offsets topics.

```bash
kafka-topics \
--create \
--bootstrap-server kafka:9092 \
--partitions 6 \
--replication-factor 1 \
--topic vehicle-positions
```