# Kafka

Projeto criado para experimentar e fazer POCs com o kafka

# Kafka Broker

## Subindo Cluster Kafka com Zookeeper

Docker compose file - Zookeeper + um nó

```
services:
  zookeeper:
    image: wurstmeister/zookeeper:latest
    ports:
      - "2181:2181"

  kafka:
    image: wurstmeister/kafka:latest
    ports:
      - "9092:9092"
    expose:
      - "9093"
    environment:
      KAFKA_ADVERTISED_LISTENERS: INSIDE://kafka:9093,OUTSIDE://localhost:9092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: INSIDE:PLAINTEXT,OUTSIDE:PLAINTEXT
      KAFKA_LISTENERS: INSIDE://0.0.0.0:9093,OUTSIDE://0.0.0.0:9092
      KAFKA_INTER_BROKER_LISTENER_NAME: INSIDE
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_CREATE_TOPICS: "my-topic:1:1"
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
```

## Subindo Cluster Kafka sem Zookeeper

Docker compose file - 3 kafkas e o kafka-ui

```
version: '3'
services:
  
  kafka-1:
    image: 'bitnami/kafka:3.3.1'
    container_name: kafka-1
    environment:
      - KAFKA_ENABLE_KRAFT=yes
      - KAFKA_CFG_PROCESS_ROLES=broker,controller
      - KAFKA_CFG_CONTROLLER_LISTENER_NAMES=CONTROLLER
      - KAFKA_CFG_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9094
      - KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT
      - KAFKA_CFG_BROKER_ID=1
      - KAFKA_CFG_CONTROLLER_QUORUM_VOTERS=1@kafka-1:9094,2@kafka-2:9094,3@kafka-3:9094
      - KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://kafka-1:9092
      - ALLOW_PLAINTEXT_LISTENER=yes
      - KAFKA_KRAFT_CLUSTER_ID=r4zt_wrqTRuT7W2NJsB_GA
    ports:
      - 9192:9092

  kafka-2:
    image: 'bitnami/kafka:3.3.1'
    container_name: kafka-2
    environment:
      - KAFKA_ENABLE_KRAFT=yes
      - KAFKA_CFG_PROCESS_ROLES=broker,controller
      - KAFKA_CFG_CONTROLLER_LISTENER_NAMES=CONTROLLER
      - KAFKA_CFG_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9094
      - KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT
      - KAFKA_CFG_BROKER_ID=2
      - KAFKA_CFG_CONTROLLER_QUORUM_VOTERS=1@kafka-1:9094,2@kafka-2:9094,3@kafka-3:9094
      - KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://kafka-2:9092
      - ALLOW_PLAINTEXT_LISTENER=yes
      - KAFKA_KRAFT_CLUSTER_ID=r4zt_wrqTRuT7W2NJsB_GA
    ports:
      - 9292:9092
      
  kafka-3:
    image: 'bitnami/kafka:3.3.1'
    container_name: kafka-3
    environment:
      - KAFKA_ENABLE_KRAFT=yes
      - KAFKA_CFG_PROCESS_ROLES=broker,controller
      - KAFKA_CFG_CONTROLLER_LISTENER_NAMES=CONTROLLER
      - KAFKA_CFG_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9094
      - KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT
      - KAFKA_CFG_BROKER_ID=3
      - KAFKA_CFG_CONTROLLER_QUORUM_VOTERS=1@kafka-1:9094,2@kafka-2:9094,3@kafka-3:9094
      - KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://kafka-3:9092
      - ALLOW_PLAINTEXT_LISTENER=yes
      - KAFKA_KRAFT_CLUSTER_ID=r4zt_wrqTRuT7W2NJsB_GA
    ports:
      - 9392:9092

  kafka-ui:
    container_name: kafka-ui
    image: 'provectuslabs/kafka-ui:latest'
    ports:
      - "8080:8080"
    environment:
      - KAFKA_CLUSTERS_0_BOOTSTRAP_SERVERS=kafka-1:9092
      - KAFKA_CLUSTERS_0_NAME=r4zt_wrqTRuT7W2NJsB_GA	  
```

Subindo os containers

```
docker-compose up -d
```

Listando os containers

```
docker ps

CONTAINER ID   IMAGE                           COMMAND                  CREATED         STATUS         PORTS              NAMES
561e550ca559   bitnami/kafka:3.3.1             "/opt/bitnami/script…"   7 minutes ago   Up 7 minutes   0.0.0.0:9292->9092/tcp, :::9292->9092/tcp   kafka-2
b9bb8aa1bed6   bitnami/kafka:3.3.1             "/opt/bitnami/script…"   7 minutes ago   Up 7 minutes   0.0.0.0:9392->9092/tcp, :::9392->9092/tcp   kafka-3
70d8f398ab6f   bitnami/kafka:3.3.1             "/opt/bitnami/script…"   7 minutes ago   Up 7 minutes   0.0.0.0:9192->9092/tcp, :::9192->9092/tcp   kafka-1
b0bc38ffc4fb   provectuslabs/kafka-ui:latest   "/bin/sh -c 'java --…"   7 minutes ago   Up 7 minutes   0.0.0.0:8080->8080/tcp, :::8080->8080/tcp   kafka-ui
```

Parar os containers

```
docker-compose stop
```

Parar e remover containers
```
docker-compose down
```

## Testando producao e consumo de mensagens nas partições

Criando um topico "topic-a" com duas partições e replicando estas duas partições 2 vezes em cada um dos kafka nodes

```
docker exec -it kafka-3 /opt/bitnami/kafka/bin/kafka-topics.sh --create --topic topic-a --partitions 2 --replication-factor 2 --if-not-exists --bootstrap-server localhost:9092
Created topic topic-a.
```

Descrevendo o conteudo de um topico

```
docker exec -it kafka-3 /opt/bitnami/kafka/bin/kafka-topics.sh --describe --topic topic-a  --bootstrap-server localhost:9092
Topic: topic-a  TopicId: 2YfqEdmvRIaNlJ1bwPGwZw PartitionCount: 2       ReplicationFactor: 2    Configs:
        Topic: topic-a  Partition: 0    Leader: 1       Replicas: 1,2   Isr: 1,2
        Topic: topic-a  Partition: 1    Leader: 2       Replicas: 2,3   Isr: 2,3
```

Enviando uma mensagem para o topico (sem especificar partição), é feito round roubin

```
docker exec -it kafka-3 /opt/bitnami/kafka/bin/kafka-console-producer.sh --broker-list localhost:9092 --topic topic-a
Msg A
Msg B
Msg C
```

Consumindo a mensagem do topic sem especificar particao (desde o começo --from-beginning)

```
docker exec -it kafka-3 opt/bitnami/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic topic-a --from-beginning
Msg A
Msg B
Msg C
```

Enviando mensagens com chave para o kafka garantir a ordenação das mensagens que possuem a mesma chave

```
#Use --property "parse.key=true" --property "key.separator=:"
docker exec -it kafka-3 /opt/bitnami/kafka/bin/kafka-console-producer.sh --broker-list localhost:9092 --topic topic-a --property "parse.key=true" --property "key.separator=:"
chave1:A
chave1:B
chave2:C
chave2:D
chave3:E
chave3:F
```

Consumindo mensagens da particao 0 e da particao 1 para ver a distribuicao das mensagens

```
#Use --partition para indicar qual partição
#Use --offset para indicar a partir de qual menasgem irá ler
docker exec -it kafka-3 opt/bitnami/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic topic-a --partition 0 --offset 0
C
D
E
F

#Particao 1 esta com a A e B
docker exec -it kafka-3 opt/bitnami/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic topic-a --partition 1 --offset 0
A
B
```

Se uma nova mensagem com chave1 for usada, será incluida na partição 1 pelo kafka

## Operations

### Deletando topico

```
docker exec -it kafka-3 opt/bitnami/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092  --delete --topic topic-a
```

### Associando consumidores em um grupo

Considerando o cenario do topic-a com 2 particoes, se forem conectados 3 consumidores no consumer group o kafka automaticamente vai designar cada consumidor para uma particao sendo que um deles vai ficar  ocioso

```
#Use o comando --group <nome> do consumer group

#Primeiro leu de uma partiacao
docker exec -it kafka-3 opt/bitnami/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic topic-a --group meu-grupo
A
B

#Segundo leu de outra
docker exec -it kafka-3 opt/bitnami/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic topic-a --group meu-grupo
C
D

#Terceiro nao leu de nada
docker exec -it kafka-3 opt/bitnami/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic topic-a --group meu-grupo
```

Entretanto ao derrubar um consumidor, o terceiro passa a assumir uma partição

```
#Terceiro passa a receber as mensagens novas sendo geradas na particao 1
docker exec -it kafka-3 opt/bitnami/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic topic-a --group meu-grupo
A1
```

Se ficar apenas um unico consumidor ele le das duas particoes


### Gerenciando consumer groups

Possivel ver os consumidores ativos e qual offset em qual partiacao

```
#user kafka-consumer-groups.sh com --describe e --group <nome do group>
docker exec -it kafka-3 opt/bitnami/kafka/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092  --describe --group meu-grupo
GROUP           TOPIC           PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG             CONSUMER-ID                                           HOST            CLIENT-ID
meu-grupo       topic-a         1          5               5               0               console-consumer-2f1e86b0-ce05-44db-8e58-bbffff966b63 /172.20.0.2     console-consumer
meu-grupo       topic-a         0          10              10              0               console-consumer-1a3c3859-fd83-4d0d-abe7-0522d0b41de3 /172.20.0.2     console-consumer
pauloapf@c11-va0f3q1qbd6:~/kafka$
```

Adicionando a opção members e verbose da para ver todos os conectados e em quais topicos e com quantas particoes

```
docker exec -it kafka-3 opt/bitnami/kafka/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092  --describe --group meu-grupo
GROUP           CONSUMER-ID                                           HOST            CLIENT-ID        #PARTITIONS     ASSIGNMENT
meu-grupo       console-consumer-2f1e86b0-ce05-44db-8e58-bbffff966b63 /172.20.0.2     console-consumer 1               topic-a(1)
meu-grupo       console-consumer-1a3c3859-fd83-4d0d-abe7-0522d0b41de3 /172.20.0.2     console-consumer 1               topic-a(0)
meu-grupo       console-consumer-438cf6fe-8dc1-4ef9-9ede-52f423bd0379 /172.20.0.2     console-consumer 0               -
```

Ao deixar apenas um consumidor ele vai ler das duas particoes

```
GROUP           CONSUMER-ID                                           HOST            CLIENT-ID        #PARTITIONS     ASSIGNMENT
meu-grupo       console-consumer-2f1e86b0-ce05-44db-8e58-bbffff966b63 /172.20.0.2     console-consumer 2               topic-a(0,1)
```
Possivel ver quantos membros tem e se está estável

```
docker exec -it kafka-3 opt/bitnami/kafka/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092  --describe --group meu-grupo -state

GROUP                     COORDINATOR (ID)          ASSIGNMENT-STRATEGY  STATE           #MEMBERS
meu-grupo                 kafka-2:9092 (2)          range                Stable          1
```

### Verificando funcionamento do replica factor

Criando um topico com 1 particao e replica factor de 2 em um cluster de 3 kafkas

```
docker exec -it kafka-3 /opt/bitnami/kafka/bin/kafka-topics.sh --create --topic topic-a --partitions 1 --replication-factor 2 --if-not-exists --bootstrap-server localhost:9092
Created topic topic-a.
```

Publiquei algumas mensagens

```
docker exec -it kafka-3 /opt/bitnami/kafka/bin/kafka-console-producer.sh --broker-list localhost:9092 --topic topic-a
A
B
C
D
```

Possivel ver pelo comando describe que as 2 replicas estao sincronizadas e o lider é o 3

```
Topic: topic-a  TopicId: AcG6kUG_SB-vNnHN8AmWTg PartitionCount: 1       ReplicationFactor: 2    Configs:
        Topic: topic-a  Partition: 0    Leader: 3       Replicas: 3,1   Isr: 3,1
```

Derrubei 2 dos 3 kafkas

```
docker stop kafka-2
docker stop kafka-3
```

Possivel ver pelo comando describe que as 1 replica esta sincronizadas e o lider é o 1

```
Topic: topic-a  TopicId: AcG6kUG_SB-vNnHN8AmWTg PartitionCount: 1       ReplicationFactor: 2    Configs:
        Topic: topic-a  Partition: 0    Leader: 1       Replicas: 3,1   Isr: 1
``` 

As mensagens ainda estão lá
```
docker exec -it kafka-1 opt/bitnami/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic topic-a --from-beginning
A
B
C
D
```

## Kafka Ui

Acessar http://localhost:8080/

