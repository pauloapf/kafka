package com.pauloapf.kafka;

import lombok.Getter;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.PartitionOffset;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import java.util.concurrent.CountDownLatch;

@Service
public class MessageListener {

    @Getter
    private CountDownLatch latch = new CountDownLatch(3);

//    @KafkaListener(topics = "topic-a", groupId = "foo")
//    public void listenGroupFoo(@Payload String message,
//                               @Header(KafkaHeaders.RECEIVED_PARTITION) int partition
//                               //@Header(KafkaHeaders.RECEIVED_KEY) String messageKey
//                                ) {
//        System.out.println("Received Message in group foo: " + message + " | partition "+partition);
//        latch.countDown();
//    }

//    @KafkaListener(topicPartitions = @TopicPartition(topic = "topic-a", partitions={"0"}))
//    public void listenGroupFooForPartition0(@Payload String message,
//                                            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition
//    ) {
//        System.out.println("Lendo apenas particao 0: " + message + " | partition "+partition);
//    }

    @KafkaListener(topicPartitions = @TopicPartition(topic = "topic-a", partitionOffsets = {@PartitionOffset(partition = "0", initialOffset = "0")}))
    public void listenGroupFooForPartition0AndOffset0(@Payload String message,
                                            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                            @Header(KafkaHeaders.OFFSET) int offset
    ) {
        System.out.println("Lendo apenas particao 0 desde o offset 0: " + message + " | partition "+partition +" |  offset" +offset);
    }

}
