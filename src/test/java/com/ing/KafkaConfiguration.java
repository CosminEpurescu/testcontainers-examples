package com.ing;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@TestConfiguration
public class KafkaConfiguration {

  @Bean
  public KafkaContainer kafkaContainer() {
    log.info("here2");
    KafkaContainer kafkaContainer =
        new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:6.2.1"));

    kafkaContainer.start();

    // setting the properties for the consumer
    System.setProperty(
        "spring.kafka.consumer.bootstrap-servers", kafkaContainer.getBootstrapServers());
    System.setProperty("spring.kafka.consumer.group-id", "Group1");

    // setting the properties for the producer
    System.setProperty(
        "spring.kafka.producer.bootstrap-servers", kafkaContainer.getBootstrapServers());

    return kafkaContainer;
  }

  @Bean
  ConcurrentKafkaListenerContainerFactory<Integer, String> kafkaListenerContainerFactory(KafkaContainer kafkaContainer) {
    ConcurrentKafkaListenerContainerFactory<Integer, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory(kafkaContainer));
    return factory;
  }

  @Bean
  public ConsumerFactory<Integer, String> consumerFactory(KafkaContainer kafkaContainer) {
    return new DefaultKafkaConsumerFactory<>(consumerConfigs(kafkaContainer));
  }

  @Bean
  public Map<String, Object> consumerConfigs(KafkaContainer kafkaContainer) {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "baeldung");
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    return props;
  }

  @Bean
  public ProducerFactory<String, String> producerFactory(KafkaContainer kafkaContainer) {
    Map<String, Object> configProps = new HashMap<>();
    configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
    configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    return new DefaultKafkaProducerFactory<>(configProps);
  }

  @Bean
  public KafkaTemplate<String, String> kafkaTemplate(KafkaContainer kafkaContainer) {
    return new KafkaTemplate<>(producerFactory(kafkaContainer));
  }

  @Bean
  public KafkaAdmin kafkaAdmin(KafkaContainer kafkaContainer) {
    log.info("here1");
    Map<String, Object> configs = new HashMap<>();
    configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
    return new KafkaAdmin(configs);
  }

  @Bean
  public NewTopic createTopic(@Value("${test.topic}") String topicName) {
    log.info("here");
    return TopicBuilder.name(topicName).build();
  }
}
