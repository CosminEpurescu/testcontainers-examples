//package com.ing;
//
//import org.junit.jupiter.api.extension.AfterAllCallback;
//import org.junit.jupiter.api.extension.BeforeAllCallback;
//import org.junit.jupiter.api.extension.ExtensionContext;
//import org.springframework.kafka.core.KafkaAdmin;
//import org.testcontainers.containers.KafkaContainer;
//import org.testcontainers.utility.DockerImageName;
//
//import java.util.Map;
//
//public class InfrastructureExtension implements BeforeAllCallback, AfterAllCallback {
//
//    private KafkaContainer kafkaContainer;
//
//    @Override
//    public void beforeAll(ExtensionContext extensionContext) throws Exception {
//        kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:6.2.1"));
//
//        kafkaContainer.start();
//
//        // setting the properties for the consumer
//        System.setProperty("spring.kafka.consumer.bootstrap-servers", kafkaContainer.getBootstrapServers());
//        System.setProperty("spring.kafka.consumer.group-id", "Group1");
//
//        // setting the properties for the producer
//        System.setProperty("spring.kafka.producer.bootstrap-servers", kafkaContainer.getBootstrapServers());
//    }
//
//
//    @Override
//    public void afterAll(ExtensionContext extensionContext) throws Exception {
//
//    }
//}
