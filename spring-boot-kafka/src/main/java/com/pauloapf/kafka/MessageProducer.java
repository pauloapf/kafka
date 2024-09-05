package com.pauloapf.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class MessageProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value(value = "${message.topic.name:topic-a}")
    private String topicName;

    public void sendMessage(String message)  {

        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topicName, message+":"+LocalDateTime.now());

        //A API send retorna CompletableFuture
        //O método get() é bloqueante. Ele espera o término da tarefa assíncrona e retorna o valor resultante.
        //Se a tarefa não estiver concluída, o método fará o bloqueio do thread até que o resultado esteja disponível.
        //SendResult<String, String> resultado = future.get();

        //O método whenComplete() é não bloqueante. Ele aceita um callback que será executado quando a tarefa for concluída,
        // independentemente se foi bem-sucedida ou se houve uma falha.
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                System.out.println("Sent message=[" + message + "] with offset=[" + result.getRecordMetadata()
                        .offset() + "]");
            } else {
                System.out.println("Unable to send message=[" + message + "] due to : " + ex.getMessage());
            }
        });
    }

    public void sendMessageToPartition(String message, int partition) {
        kafkaTemplate.send(topicName, partition, null, message);
    }

    public void sendMessageWithKey(String message, String key) {
        kafkaTemplate.send(topicName, key, message);
    }

}
