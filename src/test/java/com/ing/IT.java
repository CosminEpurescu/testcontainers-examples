package com.ing;

import com.ing.kafka.KafkaConsumer;
import com.ing.kafka.KafkaProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.testcontainers.shaded.org.hamcrest.CoreMatchers.containsString;
import static org.testcontainers.shaded.org.hamcrest.MatcherAssert.assertThat;

@Testcontainers
@Import(KafkaConfiguration.class)
// @ExtendWith(InfrastructureExtension.class)
@SpringBootTest(classes = Application.class)
@DirtiesContext
public class IT {

  @Autowired private KafkaConsumer kafkaConsumer;

  @Autowired private KafkaProducer kafkaProducer;

  @Value("${test.topic}")
  private String topic;

  @Test
  public void test() throws InterruptedException {
    String data = "lorem ipsum";

    kafkaProducer.send(topic, data);

    boolean messageConsumed = kafkaConsumer.getLatch().await(10, TimeUnit.SECONDS);

    assertFalse(messageConsumed);
    assertThat(kafkaConsumer.getPayload(), containsString(data));
  }
}
