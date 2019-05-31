package com.opencortex.kafka.tutorial1;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class ConsumerDemoGroups {

    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(ConsumerDemoGroups.class.getName());

        Properties properties = new Properties();

        String boostarpserver = "127.0.0.1:9092";
        String groupId = "my-fourth-application";
        String topic = "first_topic";

        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, boostarpserver);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        //manual offset commit
        properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        //limit the number of records
        properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "10");


        //create a consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(properties);

        //subscribe consumer to out topics
        consumer.subscribe(Arrays.asList(topic));


        //pool for new data
        while(true){
            ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofMillis(100));

            for( ConsumerRecord record : consumerRecords){

                logger.error("key: " + record.key() + ", Value: " + record.value());
                logger.error("Partition: " + record.partition() + ", Offset: " + record.offset());

                //2 strategies
                //kafka generic ID
                String id = record.topic() + "_" + record.partition() + "-" + record.offset();
                //or get the id directly from what is being consumed

                //this is to make it idempotent
                logger.error("id :" + id);
            }

            //commit the offset
            consumer.commitSync();
            logger.error("offset have been committed");
        }

    }
}
