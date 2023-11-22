#!/usr/bin/env bash
echo Waiting for Kafka to be ready...

echo Creating the topics...

kafka-topics --bootstrap-server localhost:9092 \
    --create \
    --topic vehicle-positions \
    --partitions 6 \
    --replication-factor 1

kafka-topics --bootstrap-server localhost:9092 \
    --create \
    --topic vehicle-positions-avro \
    --partitions 6 \
    --replication-factor 1

kafka-topics --bootstrap-server localhost:9092 \
    --create \
    --topic operators \
    --partitions 1 \
    --replication-factor 1

kafka-topics --bootstrap-server localhost:9092 \
    --create \
    --topic vehicle-positions-oper-47 \
    --partitions 6 \
    --replication-factor 1

kafka-topics --bootstrap-server localhost:9092 \
    --create \
    --topic tram-door-status-changed \
    --partitions 6 \
    --replication-factor 1

cat << EOF | kafka-console-producer \
    --broker-list localhost:9092 \
    --topic operators \
    --property "parse.key=true" \
    --property "key.separator=,"\
    "6","Oy Pohjolan Liikenne Ab"\
    "12","Helsingin Bussiliikenne Oy"\
    "17","Tammelundin Liikenne Oy"\
    "18","Pohjolan Kaupunkiliikenne Oy"\
    "19","Etelä-Suomen Linjaliikenne Oy"\
    "20","Bus Travel Åbergin Linja Oy"\
    "21","Bus Travel Oy Reissu Ruoti"\
    "22","Nobina Finland Oy"\
    "36","Nurmijärven Linja Oy"\
    "40","HKL-Raitioliikenne"\
    "45","Transdev Vantaa Oy"\
    "47","Taksikuljetus Oy"\
    "51","Korsisaari Oy"\
    "54","V-S Bussipalvelut Oy"\
    "55","Transdev Helsinki Oy"\
    "58","Koillisen Liikennepalvelut Oy"\
    "59","Tilausliikenne Nikkanen Oy"\
    "90","VR Oy"
EOF
