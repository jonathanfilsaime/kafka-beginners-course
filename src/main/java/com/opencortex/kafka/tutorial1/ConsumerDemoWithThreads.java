package com.opencortex.kafka.tutorial1;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class ConsumerDemoWithThreads {

    public static void main(String[] args) {




        String boostarpserver = "127.0.0.1:9092";
        String groupId = "my-sixth-application";
        String topic = "first_topic";

    }

    public class ConsumerThread implements Runnable {

        private CountDownLatch latch;
        private KafkaConsumer<String, String> consumer;
        private Logger logger = LoggerFactory.getLogger(ConsumerThread.class.getName());

        public ConsumerThread(CountDownLatch latch, String topic, String boostarpserver, String groupId){
            this.latch = latch;

            Properties properties = new Properties();
            properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, boostarpserver);
            properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
            properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

            //create a consumer
            KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(properties);

            //subscribe consumer to out topics
            consumer.subscribe(Arrays.asList(topic));
        }

        @Override
        public void run() {
            try{
                //pool for new data
                while(true){
                    ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofMillis(100));

                    for( ConsumerRecord record : consumerRecords){
                        logger.error("key: " + record.key() + ", Value: " + record.value());
                        logger.error("Partition: " + record.partition() + ", Offset: " + record.offset());
                    }
                }

            } catch (WakeupException e) {
                logger.error("recieve shutdown signal!");
            } finally {
                consumer.close();
                //tell the main code we are done
                latch.countDown();
            }

        }

        public void shutdown(){
            //special method to interrupt consumer.poll()
            //it will throw a wakeup exception
            consumer.wakeup();

        }
    }
}
