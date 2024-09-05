package com.pauloapf.kafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class SpringBootKafkaApplication {

	public static void main(String[] args) throws InterruptedException {
		ConfigurableApplicationContext context = SpringApplication.run(SpringBootKafkaApplication.class, args);

		MessageProducer producer = context.getBean(MessageProducer.class);
		MessageListener listener = context.getBean(MessageListener.class);

		//Enviando mensagens indepedente da particao
//		producer.sendMessage("Mensagem A");
//		producer.sendMessage("Mensagem B");
//		producer.sendMessage("Mensagem C");

		//Enviando mensagens para uma partição especifica
//		producer.sendMessageToPartition("A0", 0);
//		producer.sendMessageToPartition("B1", 1);

		//Enviando mensagens com chave para direcionaemnto na partição
		producer.sendMessageWithKey("A1", "chave1");
		producer.sendMessageWithKey("A2", "chave1");
		producer.sendMessageWithKey("B1", "chave2");
		producer.sendMessageWithKey("B2", "chave2");
		producer.sendMessageWithKey("C1", "chave3");

		listener.getLatch().await(1, TimeUnit.MINUTES);

		System.out.println("Finish");
	}

}
