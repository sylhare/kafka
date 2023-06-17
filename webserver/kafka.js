const os = require('os');
const Kafka = require('node-rdkafka');
const topics = [
  'driver-positions',
];

const stream = Kafka.createReadStream({
  'group.id': `${os.hostname()}`,
  'metadata.broker.list': 'kafka:9092',
  'plugin.library.paths': 'monitoring-interceptor',
}, {'auto.offset.reset': 'earliest'}, {
  topics: topics,
  waitInterval: 0,
});

stream.on('data', function(data) {
  const arr = data.value.toString().split(',');
  const message = {
    'topic': data.topic,
    'key': data.key.toString(),
    'latitude': parseFloat(arr[0]).toFixed(6),
    'longitude': parseFloat(arr[1]).toFixed(6),
    'timestamp': data.timestamp,
    'partition': data.partition,
    'offset': data.offset,
  };
  io.sockets.emit('new message', message);
});
