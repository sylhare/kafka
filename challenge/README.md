## Challenge

Example with kafka consumer, producer, stream and avro in Kotlin.
There are a couple of examples about:
- a tram (trains) system where the status of the door is being open or closed
- the position of a fleet of vehicles and drivers.
- a basic producer/consumer using a service class and how to test it.

### Get started

Start the docker images:

```shell
docker compose up
```

Create the topic, make sure that kafka is installed first, or you will see `kafka-topics command not found`.

```shell
# brew install kafka    # To install kafka to run the script
# Run the script creating the topics
./start.sh
```

Start the producers, consumers or streams, and you should see them interact. 

