Topics: a particular stream of data
    - similar to a table
    - as many topic as you want
    - identified by name

Topics are split in partitions
    - each partition is ordered
    - each message within a partition gets an incremental id called offset

Brokers
    - Kafka cluster is composed of brokers
    - each broker has an id
    - each broker contains certain topic partition
    - connecting to a broker means connecting to a cluster

Topic replication factor
    - replication factor > 1 (usually between 1 and 2)
    - leader (a broker is a leader and others are replica ISR(in-sync replica))
    - if a broker gets lost a new leader is assign

Producers
    - producers write data to topics
    - they automatically know which broker and partition to write to
    - Producers anc choose to receive acknowledgment
        - acks = 0 producer will not wait for acknowledgment (possible data loss)
        - acks = 1 default producer will wait for the leader to acknowledge
        - acks = all Leader + replicas have to acknowledge (no data loss)
     - producers can send a key
        - if key is not sent the data will be round robin
        - if a key is sent, all message with that key will go to the same partition
        - key can be string or number
        - key is necessary for data ordering

Consumers
    - read data from a topic
    - consumers know which broker to read from
    - data will be read in order over each partition

Consumer group
    - each consumer within a group will read data from exclusive partitions
    - if there are more consumers than partition a consumer will be inactive
    - usually have same number of consumer aas partition

Consumer Offsets
    - kafka stores the offset that a consumer group reads
    - the offsets committed lives in a kafka topic named _consumer_offsets
    - when a consumer in group has processed data received from kafka it should be committing the offsets

Delivery Semantics
    - consumers chose when they commit their offsets
        - at most once (not recommended)
            - offsets are committed as soon as the message is received
            - if the processing goes wrong the message will be lost forever

        - at least once (preferred)
            - commits the offsets after the message has been processed
            - if the processing goes wrong it wil read it again
            - this can result in duplicate processing of messages. Need to make sure processing is idempotent

        - exactly once
            - can only be achieved for kafka -> kafka workflow using kafka stream api

Kafka Broker Discovery
    - every broker is a bootstrap server
    - once you are connected to a broker you are also connected to all other broker
    - brokers know about all other brokers topics and partitions

zookeeper
    - manages the brokers
    - performs leader election
    - kafka can't work without zookeeper

Kafka guarantees
    - messages are appended in the order they are sent
    - consumers read message in the order they are stored
    - with replication factor of N producers and consumers can tolerate up to N-1 brokers being down
    - as long as the y number of topic remains constant for a topic the same key will always go to the same partition

Start zookeeper
    - inside /Users/plp2675/kafka_2.12-2.0.0 zookeeper-server-start.sh config/zookeeper.properties

Start Kafka
    - inside /Users/plp2675/kafka_2.12-2.0.0 kafka-server-start.sh config/server.properties

Kafka commands
    - create a topic
        kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic first_topic --create --partitions 3 --replication-factor 1

    - list all topics
        kafka-topics.sh --zookeeper 127.0.0.1:2181 --list

    - delete a topic
        kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic second_topic --delete

    - describe a topic
        kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic first_topic --describe

    - produce messages
        kafka-console-producer.sh --broker-list 127.0.0.1:9092 --topic first_topic

    - set producer properties
        kafka-console-producer.sh --broker-list 127.0.0.1:9092 --topic first_topic --producer-property acks=all

    - producing to a topic that does not exist creates a new topic
        kafka-console-producer.sh --broker-list 127.0.0.1:9092 --topic new_topic --> always creates a topic before sending a message

    - producing a message with a key
        kafka-console-producer.sh --broker-list 127.0.0.1:9092 --topic first_topic --property parse.key=true key.separator=,
        > key,value
        > another key, another value
        
    - start a consumer
        kafka-console-consumer.sh --bootstrap-server 127.0.0.1:9092 --topic first_topic

    - start a consumer and read all message from the beginning
        kafka-console-consumer.sh --bootstrap-server 127.0.0.1:9092 --topic first_topic --from-beginning

    - consume messages with the key
        kafka-console-consumer.sh --bootstrap-server 127.0.0.1:9092 --topic first_topic --from-beginning --property print.key=true --property key.separator=,

    - view all consumer groups
        kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list

    - describe consumer group
        kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group my-first-application

    - reset the offset for consumer group
        kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group my-first-application --reset-offsets --to-earliest --execute --topic first_topic

    - shift the offset forward for consumer group
        kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group my-first-application --reset-offsets --shift-by 2 --execute --topic first_topic

    - shift the offset backwards for consumer group
        kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group my-first-application --reset-offsets --shift-by -2 --execute --topic first_topic


