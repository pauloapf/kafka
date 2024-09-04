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

Para e remover containers
```
docker-compose down
```

Criando um topico "topic-a" com duas partições e replicando estas duas partições 2 vezes em cada um dos kafka nodes

```
docker exec -it kafka-3 /opt/bitnami/kafka/bin/kafka-topics.sh --create --topic topic-a --partitions 2 --replication-factor 2 --if-not-exists --bootstrap-server localhost:9092
Created topic topic-a.
```




