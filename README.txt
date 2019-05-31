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

Topic configuration
    - min.insync.replicas can be set at the broker level or the topic level
    - min.insync.replicas=2 implies 2 brokers  (including the leader) must respond that they have the data
    - that means if replication.factor=3, min.insyc.replicas=2, acks=all we can only tolerate 1 broker going down

Producers
    - producers write data to topics
    - they automatically know which broker and partition to write to
    - Producers has to choose to receive acknowledgment
        - acks = 0 producer will not wait for acknowledgment (possible data loss)
        - acks = 1 default producer will wait for the leader to acknowledge
        - acks = all Leader + replicas have to acknowledge (no data loss - recommended)
    - producers can send a key
        - if key is not sent the data will be round robin
        - if a key is sent, all message with that key will go to the same partition
        - key can be string or number
        - key is necessary for data ordering
    - configuration
        - ideally replication.factor=3, min.insync=2, acks=all
        - the broker has to be configure to at least min.insync.replicas=2
            - for instance if : replication.factor=3, min.insync=2, acks=all we can only tolerate on broker going down
        - retries setting by default is 0 (there could be an ordering problem when retrying)
        - to ensure ordering set max.in.flight.requests.per.connection to 1
        - idempotent producer
            - to set it producerProps.put("enable.idempotence", true) this implies everything below
                - kafka >= 0.11
                - retries = Integer.MAX_VALUE (2,147,483,647)
                - mac.in.flight.requests= 1 (kafka >= 0.11 & < 1.1)
                - max.in.flight.requests= 5 (Kafka > 1.1)
                - acks=all
        - use compression i.e. snappy gzip
        - linger.ms time to wait before sending the message this increases throughput linger.ms=5
        - batch.size max number of byte before sending
            - default is 16kb is fine 32kb and 64kb is ok but no more
    - keys are hashed using murmu2

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
    - preferred commit strategy enable.auto.commit=false and synchronous processing of batches
        - we control when we commit the offset

Delivery Semantics
    - consumers chose when they commit their offsets
        - at most once (not recommended)
            - offsets are committed as soon as the message is received
            - if the processing goes wrong the message will be lost forever

        - at least once (preferred)
            - commits the offsets after the message has been processed
            - if the processing goes wrong it wil read it again
            - this can result in duplicate processing of messages. Need to make sure processing is idempotent
                - 2 strategies:
                    - extract a kafka generic id String id = record.topic() + "_" + record.partition() + "-" + record.offset()
                    - extract an id from the record itself

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

considerations
    - the two most important config partition count and replication factor
        - each partition can handle a few mb/second (need to know how much a partition can handle in your system)
        - more partition implies:
            - better parallelism, better throughput
            - ability to run more consumers in a group to scale
            - ability to leverage more brokers if you have a large cluster
            - but more elections to perform for zookeeper
            - but more files to opened on kafka

guidelines
    - partitions per topic
        - small cluster (<6 brokers) 2 x number of brokers
        - big cluster (> 12 broker) 1 x number of brokers
        - adjust for the number of consumer need to run in parallel at peak throughput
        - adjust for the number fo producers throughput (increase if the super-high throughput or projected increase)
        - test
        - don't be stupid don't create 1000 partition
    - replication factor
        - at least 2, usually 3, maximum 4
        - the higher the replication factor (n):
            - better resilience of the system (N-1 broker can fail)
            - but more replication higher latency
            - but more disk space needed on the system
        - set to 3 you must have at least 3 broker
        - if replication performance is an issue get a better broker/hardware
        - never set the replication factor to  1
    - clusters
        - a broker should not have more than 2000 to 4000 partitions
        - cluster max 20,000

////////////////////////////////////////commands////////////////////////////////////////////////////////////////////////

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

    - set topic min.insync.replicas
        kafka-configs --zookeeper 127.0.0.1:2181 --entity-type topics --entity-name highly-durable --alter --add-config
        min.insync.replicas=2

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


